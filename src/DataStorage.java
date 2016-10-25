import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
//import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

/**
 * 
 */

/**
 * @author Angor
 *
 */
public class DataStorage
{
	private Hashtable< Integer, Long > hashIndices = new Hashtable< Integer, Long >();
	private ArrayList< Long > offsets = new ArrayList< Long >();
	private Hashtable< String, String > fileNames = new Hashtable< String, String >();
	private boolean encrypted = false;
	private Class<?> dataType;
	private RandomAccessFile storage = null;
	private ArrayList<Index> indices = new ArrayList< Index >();

	private boolean createStorage()
	{
		try
		{
			String typeName = dataType.getName(); 
			File file = new File(fileNames.get("storage"));
			boolean exists = file.exists();
			file.createNewFile();
			storage = new RandomAccessFile(file, "rw");
			if (exists)
			{
				storage.readInt();
				typeName = storage.readUTF();
				if (!dataType.getName().equalsIgnoreCase(typeName))
				{
					dataType = null;
					dataType = Class.forName(typeName);
				}
				return false;
			}
			// Пишем заголовок.
			storage.writeInt(0); // Смещение первой записи (пока 0)
			storage.writeUTF(typeName);
			int dataOffset = (int)storage.getFilePointer();
			storage.seek(0);
			storage.writeInt(dataOffset);
		} catch (Exception e) { return false; }
		return true;
	}

	private int reloadHashIndex(File file, Hashtable<Integer, Long> hash)
	{
		int cnt = 0;
		hash.clear();
		try
		{
			DataInputStream is = new DataInputStream(new FileInputStream(file));
			try
			{
				is.readUTF();
				while (true)
				{
					int hv = is.readInt();
					long offset = is.readLong();
					hash.put(hv, offset);
					cnt++;
				}
			} catch (EOFException eofe) {}
			is.close();
		} catch (Exception e) {}
		return cnt;
	}

	private int reloadIndex(File file, ArrayList<Long> list)
	{
		int cnt = 0;
		list.clear();
		try
		{
			DataInputStream is = new DataInputStream(new FileInputStream(file));
			try
			{
				is.readUTF();
				while (true)
				{
					long offset = is.readLong();
					list.add(offset);
					cnt++;
				}
			} catch (EOFException eofe) {}
			is.close();
		} catch (Exception e) {}
		return cnt;
	}

	private boolean createIndicies()
	{
		String hashFileName = fileNames.get("hashIndex");
		String offsetFileName = fileNames.get("offsets");
		try
		{
			File hashFile = new File(hashFileName);
			File offsetFile = new File(offsetFileName);
			boolean hashExists = hashFile.exists();
			boolean offsetExists = offsetFile.exists();
			hashFile.createNewFile();
			offsetFile.createNewFile();
			if (hashExists)
			{
				reloadHashIndex(hashFile, hashIndices);
			}
			else
			{
				DataOutputStream os = new DataOutputStream(new FileOutputStream(hashFile));
				os.writeUTF(dataType.getName() + ".objectHash");
				os.close();
			}
			if (offsetExists)
			{
				reloadIndex(offsetFile, offsets);
			}
			else
			{
				DataOutputStream os = new DataOutputStream(new FileOutputStream(offsetFile));
				os.writeUTF(dataType.getName() + ".offsets");
				os.close();
			}
		} catch (Exception e) { return false; }
		return true;
	}

	public DataStorage(String storageFileName, Class<?> dataType)
	{
		this.dataType = dataType;
		fileNames.put("storage", storageFileName);
		fileNames.put("hashIndex", storageFileName + ".hash.idx");
		fileNames.put("offsets", storageFileName + ".offsets");
		createStorage();
		createIndicies();
	}

	public void dispose()
	{
		hashIndices.clear();
		offsets.clear();
		try
		{
			storage.close();
			//hashStream.close();
			//offsetsStream.close();
		} catch (Exception e) {}
	}

	public Object getData(Field f, int val)
	{
		Index idx = null;
		for (int i = 0; i < indices.size(); i++)
		{
			if (indices.get(i).field == f)
			{
				idx = indices.get(i);
				break;
			}
		}
		if (idx == null)
		{
			idx = new Index(f);
			indices.add(idx);
		}
		long offset = idx.get(val);
		if (offset < 0)
			return null;
		return loadData(offset);
	}

	public Object getDataByHash(int hash)
	{
		if (storage == null || !hashIndices.containsKey(hash))
			return null;
		return loadData(hashIndices.get(hash));
	}

	public Object getData(int num)
	{
		if (storage == null || num < 0 || offsets.size() <= num)
			return null;
		return loadData(offsets.get(num));
	}

	public boolean addData(Object data)
	{
		if (data.getClass() != dataType)
			return false;
		try
		{
			byte[] buf = null;
			int hash = 0;
			//String dtn = dataType.getName();
			if (dataType.getName().equalsIgnoreCase("java.lang.String"))
			{
				hash = ((String)data).hashCode();
				buf = ((String)data).getBytes("UTF-8");
			}
			else
			{
				Method m = dataType.getMethod("serialize", new Class<?>[0]);
				buf = (byte[])m.invoke(data, new Object[0]);
				hash = Arrays.hashCode(buf);
			}
			if (hashIndices.containsKey(hash))
				return false;
			encrypt(buf);
			long offset = storage.length();
			storage.seek(offset);
			storage.writeInt(buf.length);
			storage.write(buf);
			offsets.add(offset);
			File file = new File(fileNames.get("offsets"));
			DataOutputStream os = new DataOutputStream(new FileOutputStream(file, true));
			os.writeLong(offset);
			os.close();
			hashIndices.put(hash, offset);
			file = new File(fileNames.get("hashIndex"));
			os = new DataOutputStream(new FileOutputStream(file, true));
			os.writeInt(hash);
			os.writeLong(offset);
			os.close();
			for (int i = 0; i < indices.size(); i++)
			{
				indices.get(i).add(data, offset);
			}
		} catch (Exception e) { return false; }
		return true;
	}

	public boolean markDelete(long offset)
	{
		try
		{
			storage.seek(offset);
			int len = storage.readInt();
			len = -len;
			storage.seek(offset);
			storage.writeInt(len);
		}
		catch (Exception e)
		{
			return false;
		}
		return true;
	}

	public int pack()
	{
		int cnt = 0;
		if (offsets.size() <= 0) return cnt;
//		DataOutputStream pStorage = null;
/*		try
		{
			hashStream.close();
			ageStream.close();
			String hashFileName = storageFileName + ".hix";
			String ageFileName = storageFileName + ".aix";
			File hashFile = new File(hashFileName);
			File ageFile = new File(ageFileName);
			hashStream = new DataOutputStream(new FileOutputStream(hashFile, false));
			ageStream = new DataOutputStream(new FileOutputStream(ageFile, false));
			pStorage = new DataOutputStream(new FileOutputStream("", false));
		} catch (Exception e) { return cnt; }
		for (int i = 0; i < offsets.size(); i++)
		{
			try
			{
				storage.seek(offsets.get(i));
				int len = storage.readInt();
				if (len < 0) continue;
				cnt++;
//				pStorage.writeInt(len);
			}
			catch (Exception e)
			{
			}
		}
*/		return cnt;
	}

	private Object loadData(long offset)
	{
		Object data = null;
		try
		{
			storage.seek(offset);
			int len = storage.readInt();
			if (len == 0) return data;
			if (len < 0) len = -len;
			byte[] buf = new byte[len];
			storage.read(buf);
			decrypt(buf);
			if (dataType.getName().equalsIgnoreCase("java.lang.String"))
			{
				data = new String(buf, "UTF-8");				
			}
			else
			{
				Method m = dataType.getMethod("deserialize", new Class<?>[] { byte[].class });
				//data = dataType.newInstance();
				data = m.invoke(null, new Object[] { buf });
			}
		} catch (Exception e) {}
		return data;
	}

	private void decrypt(byte[] buf)
	{
		if (encrypted)
		{
		}
	}

	private void encrypt(byte[] buf)
	{
		if (encrypted)
		{
		}
	}

	private class Index
	{
		public Field field;
		public Hashtable< Integer, Long > index;

		public Index(Field f)
		{
			index = new Hashtable< Integer, Long >();
			field = f;
			Method m = null;
			try
			{
				m = dataType.getMethod("getInt", new Class<?>[] { Class.forName("java.lang.reflect.Field") });
			} catch (Exception e) { return; }
			for (int i = 0; i < offsets.size(); i++)
			{
				try
				{
					Object obj = getData(i);
					int v = (Integer)m.invoke(obj, new Object[] { field });
					index.put(v, offsets.get(i));
				} catch (Exception e) {}
			}
/*			String s = fileNames.get("storage") + '.' + field.getName() + ".idx";
			try
			{
				File indexFile = new File(s);
				boolean indexExists = indexFile.exists();
				indexFile.createNewFile();
				if (indexExists)
				{
					reloadHashIndex(indexFile, index);
				}
				else
				{
					DataOutputStream os = new DataOutputStream(new FileOutputStream(indexFile));
					os.writeUTF(dataType.getName() + '.' + field.getName());
					os.close();
				}
			} catch (Exception e) {}
*/		}

		public boolean add(Object data, long offset)
		{
			int v = 0;
			try
			{
				Method m = dataType.getMethod("getInt", new Class<?>[] { Class.forName("java.lang.reflect.Field") });
				v = (Integer)m.invoke(data, new Object[] { field });
			} catch (Exception e) { return false; }
			if (index.containsKey(v)) return false;
			index.put(v, offset);
			/*
			String s = fileNames.get("storage") + '.' + field.getName() + ".idx";
			File file = new File(s);
			try
			{
				DataOutputStream os = new DataOutputStream(new FileOutputStream(file, true));
				os.writeInt(v);
				os.writeLong(offset);
				os.close();
			} catch (Exception e) {}
			*/
			return true;
		}

		public long get(int key)
		{
			if (!index.containsKey(key))
				return -1;
			return index.get(key);
		}

	}

}
