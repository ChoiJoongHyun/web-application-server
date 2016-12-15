package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
        	//in 클라이언트에서 데이터가 들어오는것 out 서버가 내보내는것.
        	log.debug("start--------------------------------------------");
        	
        	String requestUrl = null;
        	int contentLength = 0;
        	
        	
        	BufferedReader rd = new BufferedReader(new InputStreamReader(in));
        	String line;
        	StringBuffer response = new StringBuffer();
        	
        	int i=0;
        	while((line = rd.readLine()) != null) {
        		log.debug("line : {} : " + line);
        		
        		if(i == 0) {
        			requestUrl = line;
        		}
        		
        		if(line.startsWith("Content-Length")) {
        			String[] values = line.split(":");
        			contentLength = Integer.parseInt(values[1].trim());
        		}
        		
        		response.append(line);
           	 	response.append('\r');
        		if (line.equals("") || line == null) {
        			break;
        		}
        		
        		i++;
        	}
        	
        	
        	log.debug("response toString : " + response.toString());
        	
        	log.debug("end--------------------------------------------");

        	log.debug("requestUrl : " + requestUrl);
        	log.debug("contentLength : " + contentLength);
        	String url = getUrl(requestUrl);
        	
        	if(requestUrl.startsWith("POST")) {
        		if(url.contains("/user/create")) {
            		postUserCreate(url, rd, contentLength);
            	}
        	} else if (requestUrl.startsWith("GET")) {
        		if(url.contains("/user/create")) {
            		getUserCreate(url);
            	}
        	}
        	
            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    private void postUserCreate(String url, BufferedReader rd, int contentLength) {
    	try {
			String queryString = IOUtils.readData(rd, contentLength);
			Map<String, String> params = HttpRequestUtils.parseQueryString(queryString);
	    	log.debug("params toString : " + params.toString());
	    	
	    	//final MyPojo pojo = mapper.map(map, MyPojo.class);
	    	User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
	    	DataBase.addUser(user);
	    	log.debug("user toString : " + user.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private void getUserCreate(String url) {
    	int index = url.indexOf("?");
    	String requestPath = url.substring(0, index);	// /user/create
    	String queryString = url.substring(index + 1);	//userId=1&password=2&name=&email=
    	Map<String, String> params = HttpRequestUtils.parseQueryString(queryString);
    	    	
    	//final MyPojo pojo = mapper.map(map, MyPojo.class);
    	User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
    	DataBase.addUser(user);
    	log.debug("user toString : " + user.toString());
    }
    
    
    
    
    
    
    private String getUrl(String url) {
    	//log.debug("getUrl url : " + url);
    	return url.split(" ")[1];
    }
    
}
