package com.nw.yamlVerification;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.esotericsoftware.yamlbeans.YamlReader;

public class Verify {

	public static void main(String[] args) throws Exception {
		Verify verify = new Verify();
		Map map = verify.getRithub("https://raw.githubusercontent.com/akizub/HelloWorld/master/yaml/mandatory.yaml#123");
		Map contact = verify.getRithub("https://raw.githubusercontent.com/akizub/HelloWorld/master/yaml/contact.yaml#123");

		System.out.println(":" + map);
		System.out.println("::" + contact);

		verify.validate(contact, map, "");
	}

	Map getRithub(String raw) throws Exception {
		URL url = new URL(raw);

		URLConnection connection = url.openConnection();
		InputStream is = connection.getInputStream();

		Reader urlreader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		YamlReader reader = new YamlReader(urlreader);

		return (Map) reader.read();
	}

	boolean validate(Map in, Map mandatory, String keys) {
		for (Iterator iterator = in.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			Object value = in.get(key);

			checkValue(in, mandatory, keys, key, value);
		}
		return true;
	}

	void checkValue(Map in, Map mandatory, String keys, String key, Object value) {
		String newkey = keys.length() > 0 ? keys + "." + key : key;
		if (value != null && value instanceof Map) {
			//System.out.println(keys + " map:" + key);
			validate((Map) value, mandatory, newkey);
		} else if (value instanceof List) {
			//System.out.println(keys + " List" + value);
			List list = (List) value;
			for (int i = 0; i < list.size(); i++) {
				Object v = list.get(i);
				checkValue(in, mandatory, keys + "." + i, "" + i, v);
			}
		} else {
			//System.out.println(newkey + " string:" + value);
		}

		Object notFoundIn=isValid(newkey, value, mandatory);
		
		System.out.println(newkey + "=" + value + ":" + ((notFoundIn==null)?" good":" not foound in: "+notFoundIn));
	}

	Object isValid(String key, Object value, Map mandatory) {
		if (value instanceof Map || value instanceof List) return null;
		String keys[] = key.split("\\.");
		Object mv = null;
		boolean found = true;
		for (int i = 0; i < keys.length; i++) {
			mv = mandatory.get(keys[i]);
			if (mv != null) {
				if (mv instanceof Map) {
					mandatory = (Map) mv;
					continue;
				} else if (mv instanceof List) {
					break;
				}
			} else {
				found = false;
			}
		}

		if (found) {
			if (mv instanceof List) {
				if (((List) mv).contains(value))
					return null;
				if (((List) mv).contains("mandatory") && value != null)
					return null;
				return mv;
			}
			if (value.equals(mv)) {
				return null;
			}
			if ("mandatory".equals(mv) && value != null)
				return null;
			return mv;
		}

		return null;
	}

}
