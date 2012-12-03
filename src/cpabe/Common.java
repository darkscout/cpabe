package cpabe;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Common
{

	/* read byte[] from inputfile */
	public static byte[] suckFile(String inputfile) throws IOException
	{
		InputStream is = new FileInputStream(inputfile);
		int size = is.available();
		byte[] content = new byte[size];

		is.read(content);

		is.close();
		return content;
	}

	/* write byte[] into outputfile */
	public static void spitFile(String outputfile, byte[] b) throws IOException
	{
		OutputStream os = new FileOutputStream(outputfile);
		os.write(b);
		os.close();
	}
}
