/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package javaapplication1;

/**
 *
 * @author Rui Trigo
 */
 
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


class ClassHUT
{
    public String className;
    public String classId; 
    public Date classDateTime;  
};
 
 
public class javaapplication1 {
 
	private final String USER_AGENT = "Mozilla/5.0";
        private static final String username = "ruivascotrigo%40hotmail.com";
        private static final String password = "a%23yQeZyMu";
        private final String loginURL = "http://m.fitnesshut.pt/includes/login.php"; //POST
        private final String bookClassURL = "http://m.fitnesshut.pt/includes/login.php"; //POST
        private final String getClassAvailabilityURL = "http://m.fitnesshut.pt/pages/aula.php?id="; //GET
        private final String getClassesURL = "http://m.fitnesshut.pt/pages/aulas.php?id="; //GET
        
        //GET http://m.fitnesshut.pt/pages/get-aulas.php?id=5&date=2014-03-20 HTTP/1.1
	// Buscar aulas de Odivelas do proprio dia http://m.fitnesshut.pt/pages/aulas.php?id=5 //GET
        // Buscar aulas de Odivelas do dia especificado http://m.fitnesshut.pt/pages/get-aulas.php?id=5&date=2014-03-20
        
        private String phpCookie = "";
        private static final long maxWaitTime = 604800; //604800 seconds is 7 days
        private static final long beforeWaitTime = 60; //60 is in secs
        private static final long bookClassPeriod = 36000; //36000 seconds is 10 hours
        
        
	public static void main(String[] args) throws Exception {
            

 
		javaapplication1 http = new javaapplication1();
                ArrayList<ClassHUT> classHUTList = new ArrayList();
                long secsToWait = maxWaitTime; //604800 is 7 days
                long timeDiff = 0;
                
                
		System.out.println("Testing 1 - Send Http GET request");
                
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
                
                
                ClassHUT classHUTa = new ClassHUT();
                classHUTa.className="Teste a";
                classHUTa.classId="40684";
                classHUTa.classDateTime=df.parse("2014-03-19 21:50");
                             
                ClassHUT classHUTb = new ClassHUT();
                classHUTb.className="Teste b";
                classHUTb.classId="40685";
                classHUTb.classDateTime=df.parse("2014-03-20 03:16");
                
                classHUTList.add(classHUTa);
                classHUTList.add(classHUTb);
                
                
                Date currentDate = new Date();
             /*
                
                for(Iterator<ClassHUT> i = classHUTList.iterator(); i.hasNext(); ) {
                    ClassHUT item = i.next();
                    timeDiff = ( item.classDateTime.getTime() - currentDate.getTime() ) / 1000 - bookClassPeriod;
                    if (timeDiff > 0 && timeDiff < secsToWait ){
                        secsToWait = timeDiff;
                    }
                    System.out.println(item);
                }
                
                if ( secsToWait == maxWaitTime){
                    System.out.println("No classes to book");
                    return; //no classes to book
                }
                
                System.out.println(secsToWait - beforeWaitTime);

                if ( secsToWait > beforeWaitTime){
                    try {
                        Thread.sleep( (secsToWait - beforeWaitTime) * 1000);
                    } catch(InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
                */
                System.out.println("Trying to login");
                if ( http.loginHUT(username, password) == false){
                    JOptionPane.showMessageDialog(null, "Login Falhou");
                    return;
                }
                
                http.getClassesHUT("5", new Date());
                
                boolean classAvailableForBooking = false;
    
                //JOptionPane.showMessageDialog(null, "AULA DISPONIVEL");
                
                do{
                    try {
                        Thread.sleep(5000);
                    } catch(InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    classAvailableForBooking = http.getClassAvailabilityHUT(classHUTa.classId);
                
                }
                while (classAvailableForBooking == false);
                
                JOptionPane.showMessageDialog(null, "AULA DISPONIVEL");
                
                /*
                boolean esgotado = false;
                
                
		do {
                    try {
                        Thread.sleep(5000);
                    } catch(InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    esgotado = http.sendGet();
                } while (esgotado==true);
                
                JOptionPane.showMessageDialog(null, "AULA DISPONIVEL");
*/
 
	}
 
        public boolean loginHUT(String user, String pass) throws Exception{
            return this.sendPost(loginURL, "email=" + user + "&password=" + pass );
        
        }
        
        public boolean getClassAvailabilityHUT(String classId) throws Exception{
            if ("".equals(phpCookie)) return false;
            //return this.sendGet(getClassURL + classId);
            String url = getClassAvailabilityURL + classId;
            
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		// optional default is GET
		con.setRequestMethod("GET");
 
		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
                con.setRequestProperty("Cookie", phpCookie);
 
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
                //Aula pode estar em 3 estados
                // Disponivel - mas ainda nao é possivel reservar
                // Disponivel - possivel reservar
                // Indisponivel e/ou esgotada
                
                String data = response.toString();
                boolean isClassSoldOut = data.contains("Esgotado");
                boolean isClassAvailable = data.contains("Disponível");
                boolean isClassBookable = data.contains("bookAula");
                
                System.out.println(isClassSoldOut);
                System.out.println(isClassAvailable);
                System.out.println(isClassBookable);
                System.out.println(response.toString());
                
                return (isClassAvailable & isClassBookable);
		//print result
		//System.out.println(response.toString());
        }
        
        public boolean getClassesHUT(String fitnessHutLocationId, Date day) throws Exception{
            if ("".equals(phpCookie)) return false;
            //return this.sendGet(getClassURL + classId);
            //&date=2014-03-20
            
            // 
            
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String url = getClassesURL + fitnessHutLocationId + "&date=" + df.format(day);
            
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		// optional default is GET
		con.setRequestMethod("GET");
 
		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
                con.setRequestProperty("Cookie", phpCookie);
 
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

        	System.out.println(response.toString());
                
                Document doc = Jsoup.parse(response.toString());
                Elements links = doc.getElementsByTag("li");
                
                for (Element link : links) {
                    Element e = link.child(0);
                    String classId = e.attr("onclick");
                    classId = classId.substring( classId.indexOf("(") + 1 , classId.lastIndexOf(")"));
                    String classSchedule = e.child(0).child(0).text();
                    String classInfo = e.child(1).child(0).text();
                    String classDurationLocation = e.child(1).child(1).text();
                    String classDuration = classDurationLocation.substring( 0 , classDurationLocation.indexOf(",") );
                    String classLocation = classDurationLocation.substring( classDurationLocation.indexOf(",") + 1 );
                    
                    System.out.println(classId);
                    System.out.println(classSchedule);
                    System.out.println(classInfo);
                    System.out.println(classDuration);
                    System.out.println(classLocation);
                }
                
                return true;
        }
        
        
        public void bookClassHUT(){
        
        
        
        }
  
	// HTTP GET request
	private boolean sendGet(String url) throws Exception {
 
		//String url = "http://www.fitnesshut.pt/myhut/pages/get-aula.php?id=40663";
                //String url = "http://www.fitnesshut.pt/myhut/pages/get-aula.php?id=40663";
                //login POST http://m.fitnesshut.pt/includes/login.php email=ruivascotrigo%40hotmail.com&password=a%23yQeZyMu
                //Reservar aula POST http://m.fitnesshut.pt/includes/myhut.php  com aula=40682&socio=52087&op=book-aula
                
                
                URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		// optional default is GET
		con.setRequestMethod("GET");
 
		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
                con.setRequestProperty("Cookie", phpCookie);
 
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
                //Aula pode estar em 3 estados
                // Disponivel - mas ainda nao é possivel reservar
                // Disponivel - possivel reservar
                // Indisponivel e/ou esgotada
                
                String data = response.toString();
                boolean isClassSoldOut = data.contains("Esgotado");
                boolean isClassAvailable = data.contains("Disponível");
                boolean isClassBookable = data.contains("bookAula");
                
                System.out.println(isClassSoldOut);
                System.out.println(isClassAvailable);
                System.out.println(isClassBookable);
                System.out.println(response.toString());
                
                
                return (isClassAvailable & isClassBookable);
		//print result
		//System.out.println(response.toString());
 
	}
        
	// HTTP POST request
	private boolean sendPost(String url, String urlParameters) throws Exception {
            
                Boolean isAuthOK = false;
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
 
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
 
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);
 
                if (con.getResponseCode() == HttpURLConnection.HTTP_OK){
                    Object o = con.getContent();
                    System.out.println("Content-Type: " + con.getContentType());
                }
                    
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
                
                if ( "  -1".equals(response.toString()) ) {
                    System.out.println("Failed Auth");
                } else if ( "  -2".equals(response.toString()) ) {
                    
                    System.out.println("Failed Auth");
                } else{
                    System.out.println("Auth OK");
                    phpCookie = con.getHeaderField("Set-Cookie");
                    isAuthOK = true;
                }
		//print result
		System.out.println(response.toString());
                System.out.println(phpCookie);
                
                return isAuthOK;
 
	}
 
}