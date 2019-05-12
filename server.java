package httpserver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import com.sun.net.httpserver.HttpServer;

public class Server {

	public static void main(String[] args) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(8500), 0);
		HttpContext context = server.createContext("/example");
		HttpContext context1 = server.createContext("/echo");
		context.setHandler(Server::handleRequest);
		context1.setHandler(Server::handleRequest1);
		server.start();
	}

	private static void handleRequest1(HttpExchange exchange) throws IOException {
		String response = "This is ECHO ";
		exchange.sendResponseHeaders(200, response.getBytes().length);
		OutputStream os = exchange.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}

	private static void handleRequest(HttpExchange exchange) throws IOException {
		URI requestURI = exchange.getRequestURI();
		String query = requestURI.getQuery();
		boolean debug=query!=null;
		
		Headers requestHeaders = exchange.getRequestHeaders();
		List<String> contentType = requestHeaders.get("Content-type");
		if (debug)System.out.println(contentType);

		String boundary = null;
		for (Iterator iterator = contentType.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			if (debug)System.out.println(string);
			int index = string.indexOf("boundary=");
			if (index >= 0) {
				boundary = string.substring(index + ("boundary=".length()));
				if (debug)System.out.println(boundary);
			}
		}

		StringBuffer sb = null;
		String name=null, filename=null;

		if (boundary != null) {
			InputStream in = exchange.getRequestBody();
			java.util.Scanner s = new java.util.Scanner(in).useDelimiter("\\R");
			;
			while (s.hasNext()) {
				String n = s.next();
				if (n.contains(boundary)) {
					if (sb == null) {
						sb = new StringBuffer();
						while (s.hasNext()) {
							if ((n = s.next()).length() == 0)
								break;
							if (debug)System.out.println(">>>" + n);
							if (n.contains("Content-Disposition:")) {
								String args[] = n.split("; ");
								for (int i = 0; i < args.length; i++) {
									String string = args[i];
									if (debug)System.out.println(string);
									if (string.contains("=")) {
										String[] pair = string.split("=");
										if (pair[0].equals("name"))
											name = pair[1].replace("\"", "");
										if (pair[0].equals("filename"))
											filename = pair[1].replace("\"", "");
									}
								}
							}

						}

						if (s.hasNext())
							n = s.next();

					}
				}
				if (n.contains(boundary))
					break;
				if (sb != null) {
					sb.append(n).append("\r");
				}

			}
			// s.close();
		}
		if (debug)System.out.println("======");
		String result="File ";
		if (sb != null) {
			if (debug)System.out.println(sb);
			File dir=new File(name);
			File file=new File(dir,filename);
			result+=file.getAbsolutePath();
			FileWriter fw=new FileWriter(file);
			fw.write(sb.toString());
			fw.close();
			result+=" written.\n";
		}
		if (debug)System.out.println("======");

		if (debug)printRequestInfo(exchange);
		String response = result;
		exchange.sendResponseHeaders(200, response.getBytes().length);
		OutputStream os = exchange.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}

	private static void printRequestInfo(HttpExchange exchange) {
		System.out.println("-- headers --");
		Headers requestHeaders = exchange.getRequestHeaders();
		requestHeaders.entrySet().forEach(System.out::println);

		System.out.println("-- principle --");
		HttpPrincipal principal = exchange.getPrincipal();
		System.out.println(principal);

		System.out.println("-- HTTP method --");
		String requestMethod = exchange.getRequestMethod();
		System.out.println(requestMethod);

		System.out.println("-- query --");
		URI requestURI = exchange.getRequestURI();
		String query = requestURI.getQuery();
		System.out.println(query);
	}

}
