package jgsc;

import java.io.*;
import java.net.*;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

public class GstoreConnector {

    public static final String defaultServerIP = "127.0.0.1";
    public static final int defaultServerPort = 9000;

    private String serverIP;
    private int serverPort;
    //private Socket socket = null;

    public GstoreConnector() {
        this.serverIP = GstoreConnector.defaultServerIP;
        this.serverPort = GstoreConnector.defaultServerPort;
    }

    public GstoreConnector(int _port) {
        this.serverIP = GstoreConnector.defaultServerIP;
        this.serverPort = _port;
    }

    public GstoreConnector(String _ip, int _port) {
        this.serverIP = _ip;
        this.serverPort = _port;
    }

	//PERFORMANCE: what if the query result is too large?  receive and save to file directly at once
	//In addition, set the -Xmx larger(maybe in scale of Gs) if the query result could be very large, 
	//this may help to reduce the GC cost
    public String sendGet(String param) {
		String url = "http://" + this.serverIP + ":" + this.serverPort;
        StringBuffer result = new StringBuffer();
        BufferedReader in = null;
		System.out.println("parameter: "+param);

		try {
			param = URLEncoder.encode(param, "UTF-8");
		}
		catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("Broken VM does not support UTF-8");
		}

        try {
            String urlNameString = url + "/" + param;
            System.out.println("request: "+urlNameString);
            URL realUrl = new URL(urlNameString);
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setConnectTimeout(10000);
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
			//set agent to avoid: speed limited by server if server think the client not a browser
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.connect();

			long t0 = System.currentTimeMillis(); //ms

            Map<String, List<String>> map = connection.getHeaderFields();

			long t1 = System.currentTimeMillis(); //ms
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
				//PERFORMANCE: this can be very costly if result is very large, because many temporary Strings are produced
				//In this case, just print the line directly will be much faster
				result.append(line+"\n");
            }

			long t2 = System.currentTimeMillis(); //ms
			//System.out.println("Time to get data: "+(t2 - t1)+" ms");
        } catch (Exception e) {
            System.out.println("error in get request: " + e);
            e.printStackTrace();
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result.toString();
    }

    public void sendGet(String param, String filename) {
        String url = "http://" + this.serverIP + ":" + this.serverPort;
        BufferedReader in = null;
        System.out.println("parameter: "+param);
        
        if (filename == null)
            return;

        FileWriter fw = null;
        try {
            fw = new FileWriter(filename);
        } catch (IOException e) {
            System.out.println("can not open " + filename + "!");
        }

        try {
            param = URLEncoder.encode(param, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Broken VM does not support UTF-8");
        }

        try {
            String urlNameString = url + "/" + param;
            System.out.println("request: "+urlNameString);
            URL realUrl = new URL(urlNameString);
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setConnectTimeout(10000);
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            //set agent to avoid: speed limited by server if server think the client not a browser
            connection.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.connect();
            
            long t0 = System.currentTimeMillis(); //ms
            
            Map<String, List<String>> map = connection.getHeaderFields();

            long t1 = System.currentTimeMillis(); // ms
            //System.out.println("Time to get header: "+(t1 - t0)+" ms");

            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            char chars[] = new char[2048];
            int b;
            while ((b = in.read(chars, 0, 2048)) != -1) {
                if (fw != null)
                    fw.write(chars);
                chars = new char[2048];
            }

            long t2 = System.currentTimeMillis(); //ms
            //System.out.println("Time to get data: "+(t2 - t1)+" ms");
        } catch (Exception e) {
            //System.out.println("error in get request: " + e);
            e.printStackTrace();
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (fw != null) {
                    fw.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return;
    }


//NOTICE: no need to connect now, HTTP connection is kept by default
    public boolean load(String _db_name, String _username, String _password) {
		boolean connect_return = this.connect();
		if (!connect_return) {
			System.err.println("connect to server error. @GstoreConnector.load");
			return false;
		}

        String cmd = "?operation=load&db_name=" + _db_name + "&username=" + _username + "&password=" + _password;
        String msg = this.sendGet(cmd);
        //if (!send_return) {
            //System.err.println("send load command error. @GstoreConnector.load");
            //return false;
        //}

        this.disconnect();

        System.out.println(msg);	
        if (msg.equals("load database done.")) {
            return true;
        }

        return false;
    }

    public boolean unload(String _db_name,String _username, String _password) {
        boolean connect_return = this.connect();
        if (!connect_return) {
            System.err.println("connect to server error. @GstoreConnector.unload");
            return false;
        }

		String cmd = "?operation=unload&db_name=" + _db_name + "&username=" + _username + "&password=" + _password;
        String msg = this.sendGet(cmd);

        this.disconnect();

        System.out.println(msg);	
        if (msg.equals("unload database done.")) {
            return true;
        }

        return false;
    }

    public boolean build(String _db_name, String _rdf_file_path, String _username, String _password) {
        boolean connect_return = this.connect();
        if (!connect_return) {
            System.err.println("connect to server error. @GstoreConnector.build");
            return false;
        }

		//TODO: also use encode to support spaces?
		//Consider change format into ?name=DBname
        String cmd = "?operation=build&db_name=" + _db_name + "&ds_path=" + _rdf_file_path  + "&username=" + _username + "&password=" + _password;;
        String msg = this.sendGet(cmd);

        this.disconnect();

        System.out.println(msg);
        if (msg.equals("import RDF file to database done.")) {
            return true;
        }

        return false;
    }

	//TODO: not implemented
    public boolean drop(String _db_name) {
        boolean connect_return = this.connect();
        if (!connect_return) {
            System.err.println("connect to server error. @GstoreConnector.drop");
            return false;
        }

        String cmd = "drop/" + _db_name;
        String msg = this.sendGet(cmd);

        this.disconnect();

        System.out.println(msg);
        return msg.equals("drop database done.");
    }

    public String query(String _username, String _password, String _db_name, String _sparql) {
        boolean connect_return = this.connect();
        if (!connect_return) {
            System.err.println("connect to server error. @GstoreConnector.query");
            return "connect to server error.";
        }

		//URL encode should be used here
		//try {
		//_sparql = URLEncoder.encode("\""+_sparql+"\"", "UTF-8");
		//}
		//catch (UnsupportedEncodingException ex) {
			//throw new RuntimeException("Broken VM does not support UTF-8");
		//}

		String cmd = "?operation=query&username=" + _username + "&password=" + _password + "&db_name=" + _db_name + "&format=txt&sparql=" + _sparql;
        //String cmd = "query/\"" + _sparql + "\"";
        String msg = this.sendGet(cmd);

        this.disconnect();

        return msg;
    }
    
    public void query(String _username, String _password, String _db_name, String _sparql, String _filename) {
        boolean connect_return = this.connect();
        if (!connect_return) {
            System.err.println("connect to server error. @GstoreConnector.query");
        }

        String cmd = "?operation=query&username=" + _username + "&password=" + _password + "&db_name=" + _db_name + "&format=json&sparql=" + _sparql;
        this.sendGet(cmd, _filename);
      
        this.disconnect();
        
        return;
    }


 //   public String show() {
  //      return this.show(false);
  //  }

	//show all databases
    public String show() {
        boolean connect_return = this.connect();
        if (!connect_return) {
            System.err.println("connect to server error. @GstoreConnector.show");
            return "connect to server error.";
        }

        String cmd = "?operation=show";
        String msg = this.sendGet(cmd);
        
        this.disconnect();
        return msg;
    }
	 public String user(String type, String username1, String password1, String username2, String addtion) {
        boolean connect_return = this.connect();
        if (!connect_return) {
            System.err.println("connect to server error. @GstoreConnector.show");
            return "connect to server error.";
        }

        String cmd = "?operation=user&type=" + type + "&username1=" + username1 + "&password1=" + password1 + "&username2=" + username2 + "&addtion=" + addtion;
        String msg = this.sendGet(cmd);
        
        this.disconnect();
        return msg;
    }
	 public String showUser() {
        boolean connect_return = this.connect();
        if (!connect_return) {
            System.err.println("connect to server error. @GstoreConnector.show");
            return "connect to server error.";
        }

        String cmd = "?operation=showUser";
        String msg = this.sendGet(cmd);
        
        this.disconnect();
        return msg;
    }
	 public String monitor(String db_name) {
        boolean connect_return = this.connect();
        if (!connect_return) {
            System.err.println("connect to server error. @GstoreConnector.show");
            return "connect to server error.";
        }

        String cmd = "?operation=monitor&db_name=" + db_name;
        String msg = this.sendGet(cmd);
        
        this.disconnect();
        return msg;
    }
	 public String checkpoint(String db_name) {
        boolean connect_return = this.connect();
        if (!connect_return) {
            System.err.println("connect to server error. @GstoreConnector.show");
            return "connect to server error.";
        }

        String cmd = "?operation=checkpoint&db_name=" + db_name;
        String msg = this.sendGet(cmd);
        
        this.disconnect();
        return msg;
    }
	public String test_download(String filepath)
	{
        boolean connect_return = this.connect();
        if (!connect_return) {
            System.err.println("connect to server error. @GstoreConnector.query");
            return "connect to server error.";
        }

		//TEST: a small file, a large file
		String cmd = "?operation=delete&download=true&filepath=" + filepath;
        String msg = this.sendGet(cmd);

        this.disconnect();

        return msg;
	}

    private boolean connect() {
		return true;
    }

    private boolean disconnect() {
		return true;
    }

    private static byte[] packageMsgData(String _msg) {
        //byte[] data_context = _msg.getBytes();
        byte[] data_context = null;
        try {
            data_context = _msg.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.err.println("utf-8 charset is unsupported.");
            data_context = _msg.getBytes();
        }
        int context_len = data_context.length + 1; // 1 byte for '\0' at the end of the context.
        int data_len = context_len + 4; // 4 byte for one int(data_len at the data's head).
        byte[] data = new byte[data_len];

        // padding head(context_len).
        byte[] head = GstoreConnector.intToByte4(context_len);
        for (int i = 0; i < 4; i++) {
            data[i] = head[i];
        }

        // padding context.
        for (int i = 0; i < data_context.length; i++) {
            data[i + 4] = data_context[i];
        }
        // in C, there should be '\0' as the terminator at the end of a char array. so we need add '\0' at the end of sending message.
        data[data_len - 1] = 0;

        return data;
    }

    private static byte[] intToByte4(int _x) // with Little Endian format.
    {
        byte[] ret = new byte[4];
        ret[0] = (byte) (_x);
        ret[1] = (byte) (_x >>> 8);
        ret[2] = (byte) (_x >>> 16);
        ret[3] = (byte) (_x >>> 24);

        return ret;
    }

    private static int byte4ToInt(byte[] _b) // with Little Endian format.
    {
        int byte0 = _b[0] & 0xFF, byte1 = _b[1] & 0xFF, byte2 = _b[2] & 0xFF, byte3 = _b[3] & 0xFF;
        int ret = (byte0) | (byte1 << 8) | (byte2 << 16) | (byte3 << 24);

        return ret;
    }

    public static void main(String[] args) {
        // initialize the GStore server's IP address and port.
        GstoreConnector gc = new GstoreConnector("127.0.0.1", 9000);

        // build a new database by a RDF file.
        // note that the relative path is related to gserver.
        //gc.build("db_LUBM10", "example/rdf_triple/LUBM_10_GStore.n3");
        String sparql = "select ?x where {"
                + "<Area_51>	<location>	?x"
                + "}";
        
        sparql = "select ?countries where { ?countries	<type>	<Country> . ?caves	<type>	<Cave> . ?caves	<location>	?countries . } " 
        		+ "GROUP BY ?countries HAVING(COUNT(?caves) > 1000)";
        
        sparql = "ASK where { <Proinsulin>	<type>	<Protein> .}";
        
        sparql = "select DISTINCT ?film ?budget  where { ?film	<type>	<Film> . ?film	<director>	<Paul_W._S._Anderson> . ?film	<budget>	?budget . }";

//        boolean flag = gc.load("dbpedia16", "root", "123456");
        //System.out.println(flag);
        String answer = gc.query("root", "123456", "dbpedia16", sparql);
        System.out.println(answer);
    }
}

