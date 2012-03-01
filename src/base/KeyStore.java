package base;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.PriorityQueue;

//NKeys goal = 100 million
//KeySet - in-memory entry array of keys by their hashcodes
//		entry array size: NKeys / 0.75 * 4 bytes = 533 Mb
//		key store size: NKeys * 21 bytes = 2100 Mb
//Key ID - ordinal number of Key -> address on disk
//PersistedKey (0/4 + 1 + 8/16 + 4 + 4 = 17/29) 
//		id - [implicit] 4 bytes
//		hashcode - 4 bytes (may not be necessary)
//		agent - 1 byte
//		boxset - 1 bit per cell (rounded to 4 bytes) ~ 128 bits = 16 bytes
//		previous key - 4 bytes
//		next key in bucket - 4 bytes
//in-memory KeyPriorityQueue:
//		key id
//		pushes
//		total

//Key Add operation:

/*
 Simplify by preallocating entry array to the largest size and disallowing resizing of hashtable.
 With this key store will be append only with random access.

 boolean add(SetEntry key)
 int hash = key.hashCode
 int index = hash & (table.length - 1)
 for SetEntry e = table[index]; e != null; e = e.next
 read key from store
 if key.equals(e)
 return false

 synchronize
 assert key.next == null
 key.next = table[index]
 write new key to store
 table[index] = key_id
 return true
 */

final class PKey {
	int agent;
	int[] boxset;
	int previousKey;
	int nextKeyInBucket;

	public int hashCode() {
		throw new RuntimeException();
	}

	public boolean equals(PKey key) {
		throw new RuntimeException();
	}

	static int size(int boxsetLength) {
		return 1 + boxsetLength * 4 + 4 + 4;
	}

	void write(ByteBuffer buffer) {
		buffer.put((byte) agent);
		for (int i = 0; i < boxset.length; i++)
			buffer.putInt(boxset[i]);
		buffer.putInt(previousKey);
		buffer.putInt(nextKeyInBucket);
	}

	void read(ByteBuffer buffer) {
		agent = buffer.get();
		for (int i = 0; i < boxset.length; i++)
			boxset[i] = buffer.getInt();
		previousKey = buffer.getInt();
		nextKeyInBucket = buffer.getInt();
	}
}

class X {
	int id;
	int pushes;
	int total;
}

class XXX {
	PriorityQueue<X> queue;
}

// Thread safe!
final class ExternalKeySet {
	private int table[];
	private final KeyStore store;

	public ExternalKeySet(int capacity, KeyStore store) {
		table = new int[1 << capacity];
		this.store = store;
	}

	public boolean add(PKey key) {
		int hash = key.hashCode();
		int index = hash & (table.length - 1);
		ByteBuffer buffer = store.createBuffer();

		synchronized (this) {
			int id = table[index];
			while (id != 0) {
				PKey x = new PKey();
				store.read(id, x, buffer);
				if (key.equals(x))
					return false;
				id = x.nextKeyInBucket;
			}

			assert key.nextKeyInBucket == 0;
			key.nextKeyInBucket = table[index];
			table[index] = store.write(key, buffer);
			return true;
		}
	}

	public synchronized void clear() {
		Arrays.fill(table, 0);
	}

	public synchronized int size() {
		return store.size();
	}
}

final class KeyStore {
	private final File file;
	private final FileChannel channel;
	private final int boxsetLength;
	private int size;

	public KeyStore(int boxsetLength) {
		try {
			file = File.createTempFile("keystore", ".tmp");
			System.out.println(file);
			channel = new RandomAccessFile(file, "rw").getChannel();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.boxsetLength = boxsetLength;
	}

	public void close() {
		try {
			channel.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		file.delete();
	}

	public int size() {
		return size;
	}

	public ByteBuffer createBuffer() {
		return ByteBuffer.allocate((int) PKey.size(boxsetLength));
	}

	public int write(PKey key, ByteBuffer buffer) {
		if (key.boxset.length != boxsetLength)
			throw new IllegalArgumentException();

		try {
			buffer.clear();
			key.write(buffer);
			buffer.flip();
			channel.write(buffer, size * buffer.capacity());
			return ++size;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public PKey read(int id, PKey key, ByteBuffer buffer) {
		if (id <= 0 || id > size || key.boxset.length != boxsetLength)
			throw new IllegalArgumentException();

		try {
			buffer.clear();
			channel.read(buffer, (id - 1) * buffer.capacity());
			buffer.rewind();
			key.read(buffer);
			return key;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		KeyStore store = new KeyStore(2);
		ByteBuffer buffer = store.createBuffer();
		try {
			PKey a = new PKey();
			a.boxset = new int[2];

			a.agent = 13;
			a.nextKeyInBucket = 15;
			System.out.printf("e=1 a=%s\n", store.write(a, buffer));
			a.agent = 14;
			a.nextKeyInBucket = 16;
			System.out.printf("e=2 a=%s\n", store.write(a, buffer));

			a.agent = 0;
			store.read(1, a, buffer);
			System.out.printf("e=13 a=%s\n", a.agent);
			System.out.printf("e=15 a=%s\n", a.nextKeyInBucket);
			store.read(2, a, buffer);
			System.out.printf("e=14 a=%s\n", a.agent);
			System.out.printf("e=16 a=%s\n", a.nextKeyInBucket);
		} finally {
			store.close();
		}
	}
}
