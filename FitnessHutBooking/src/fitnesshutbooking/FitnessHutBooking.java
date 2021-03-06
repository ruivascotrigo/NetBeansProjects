/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.

 */

package fitnesshutbooking;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import javax.mail.*;
import javax.mail.internet.*;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

/**
 *
 * @author Rui Trigo
 */
public class FitnessHutBooking {
   
    private static final String USER_AGENT = "Mozilla/5.0";
    
    private static final String loginURL = "https://www.myhut.pt/myhut/functions/login.php"; //POST
    
    //private static final String bookClassURL = "https://www.myhut.pt/myhut/functions/myhut.php"; //POST
    //Below for new mobile app
    // https://www.myhut.pt/webservices/myhut/aulas-marcacao-json.php?id=52087&aid=187137
    // private static final String bookClassURL = "https://www.myhut.pt/webservices/myhut/aulas-marcacao-json.php?id="; //GET
    private static final String bookClassURL = "https://www.myhut.pt/appservices/app2016maymyv1hut/aulas-marcacao-json.php"; //POST
    
    
    private static final String getClassAvailabilityURL = "https://www.myhut.pt/myhut/functions/get-aula.php?id="; //GET
    
    private static final String getClassesURL = "https://www.myhut.pt/myhut/functions/get-aulas.php?id="; //GET
    
    //GET http://m.fitnesshut.pt/pages/get-aulas.php?id=5&date=2014-03-20 HTTP/1.1
    // Buscar aulas de Odivelas do proprio dia http://m.fitnesshut.pt/pages/aulas.php?id=5 //GET
    // Buscar aulas de Odivelas do dia especificado http://m.fitnesshut.pt/pages/get-aulas.php?id=5&date=2014-03-20

    public static final long maxWaitTime = 604800; //604800 seconds is 7 days
    public static final long beforeWaitTime = 60; //60 is in secs
    public static final long bookClassPeriod = 36000; //36000 seconds is 10 hours

    /*
<option value="1">Amoreiras</option>
<option value="2">Cascais</option>
<option value="3">Trindade</option>
<option value="4">Arco do Cego</option>
<option value="5">Odivelas</option>
<option value="6">Braga</option>
<option value="7">Picoas</option>
<option value="8">Olivais</option>
<option value="9">Loures</option>
<option value="10">Linda a Velha</option>
<option value="11">Setúbal</option>
*/
    
    public static final String amoreirasHUT = "1";
    public static final String cascaisHUT = "2";
    public static final String trindadeHUT = "3";
    public static final String arcodocegoHUT = "4";
    public static final String odivelasHUT = "5";
    public static final String bragaHUT = "6";
    public static final String picoasHUT = "7";
    public static final String olivaisHUT = "8";
    public static final String louresHUT = "9";
    public static final String lindaavelhaHUT = "10";
    public static final String setubalHUT = "11";
  
        
    private static String USER_NAME = "*****";  // GMail user name (just the part before "@gmail.com")
    private static String PASSWORD = "********"; // GMail password
    private static String RECIPIENT = "lizard.bill@myschool.edu";

    /* INSERT THE BELOW CODE FOR EMAIL SENDING
    String from = USER_NAME;
    String pass = PASSWORD;
    String[] to = { RECIPIENT }; // list of recipient email addresses
    String subject = "Java send mail example";
    String body = "Welcome to JavaMail!";

    sendFromGMail(from, pass, to, subject, body);
    */
    
    
    public Vector<Vector> todayClasses = new Vector<Vector>();
    public Vector<Vector> tomorrowClasses = new Vector<Vector>();
    
    //private String phpCookie = "";
    private String userId = "";
    private static final String bookingPassword = "e94b10f0da8d42095ca5c20927416de5";
    
    
    public static String loginHUT(String user, String pass) throws Exception{
        
        String loginResult = "AuthFailed";
        
        String url = loginURL;
        
        //String urlParameters = "email=" + user + "&password=" + pass;
        String urlParameters = "myhut-login-email=" + user + "&myhut-login-password=" + pass;
        
        URL obj = new URL(url);
        String host = obj.getHost();
        try {
            InetAddress address = InetAddress.getByName(host);
        } catch (Exception ex) {
            //JOptionPane.showMessageDialog(null, "O teu PC é uma bosta que nem consegue resolver um nome dns, compra apple que é bom!" );
            return loginResult;
        }
        InetAddress address = InetAddress.getByName(host);
        
        String ip = address.getHostAddress();
        //JOptionPane.showMessageDialog(null, "The dns name " + host + " was resolved to the IP address " + ip );
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add request header
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
            //System.out.println("Content-Type: " + con.getContentType());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
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
            loginResult = con.getHeaderField("Set-Cookie");
        }

        System.out.println(loginResult);

        return loginResult;
    }
    
    public static String getClassAvailabilityHUT(/*String phpCookie, */String classId) throws Exception {

        String userId = "Unavailable";
        //return this.sendGet(getClassURL + classId);
        String url = getClassAvailabilityURL + classId;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);
        //con.setRequestProperty("Cookie", phpCookie);

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
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

        boolean isClassSoldOut = data.contains("Esgotada");

        boolean isClassAvailable = data.contains("disponível");

        boolean isClassBookable = data.contains("bookAula");

        System.out.println("isClassSoldOut: " + isClassSoldOut);
        System.out.println("isClassAvailable: " + isClassAvailable);
        System.out.println("isClassBookable: " + isClassBookable);

        if (isClassAvailable & isClassBookable) {
            userId = "52087";
        }

        System.out.println(userId);
        return userId;
        
    }

    public boolean getClassesHUT(String phpCookie, String fitnessHutLocationId, Date day, Vector<Vector> data) throws Exception{
       
        if ( data != null ) {data.clear();}

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
            
            if ( responseCode != HttpURLConnection.HTTP_OK) return false;

            BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream(),"UTF-8") );
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
            }
            in.close();

            System.out.println(response.toString());

            Document doc = Jsoup.parse(response.toString(),"UTF-8");
            
            //Element classList = doc.getElementById("aulas-list");
            Elements links = doc.getElementsByClass("panel-title");
            
            for (Element link : links) {
                Element e = link.child(0);
                String classId = e.attr("onclick");

                classId = classId.substring( classId.indexOf("(") + 1 , classId.lastIndexOf(")"));
                
                Node eTime = e.childNode(0).childNode(0).childNode(0);
                String classTime = eTime.toString();
                Node eName = e.childNode(1).childNode(0).childNode(0);
                String classInfo = eName.toString();
                TextNode eLocation = (TextNode) e.childNode(2).childNode(0);
                String classLocation = eLocation.text();
                Node eDuration = e.childNode(3).childNode(0);
                String classDuration = eDuration.toString();
                
                DateFormat datef = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date dia = datef.parse( df.format(day) + " " + classTime );
                
                Vector row = new Vector();
                row.add(classId);
                //row.add(classSchedule);
                row.add(dia);
                row.add(classInfo);
                row.add(classDuration);
                row.add(classLocation);
                row.add(new Boolean(false));
                data.add(row);
                

            }       
            
            return true;
    }

    public static boolean bookClassHUT(String phpCookie, String classId, String userId, Date classDate) throws Exception{
   
/*
        MARCACAO COM USO DE GET
        
        String url = bookClassURL + userId + "&aid=" + classId ;
        
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        
        
        
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        if ( response.toString().contains("Aula reservada") ) {
            System.out.println("Aula marcada com sucesso, código: ->" + response.toString() + "<-");
            return true;
        }
        System.out.println("Nao foi possivel marcar a aula, codigo de erro: ->" + response.toString() + "<-");
        return false;
*/     
        

        //MARCACAO COM USO DE POST
        
        String url = bookClassURL;
        
        String urlParameters = "id=" + userId + "&aid=" + classId + "&password=" + bookingPassword ;
        
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        
        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        //con.setRequestProperty("Cookie", phpCookie);

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

        BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
        }
        in.close();
      
        if ( response.toString().contains("Aula reservada") ) {
            System.out.println("Aula marcada com sucesso, código: ->" + response.toString() + "<-");
            return true;
        }
        System.out.println("Nao foi possivel marcar a aula, codigo de erro: ->" + response.toString() + "<-");
        return false;
    
        
     /*   
        resultados
            1 aulareservada
            -1 Não é possível reservar mais aulas! Máximo 2 aulas por dia.
            -2 Não pode reservar aulas! Não tem acesso a marcação de aulas.
            -3 Não pode reservar a aula! Aula Esgotada.
            nada nao sei
*/
        
        
/*
//Book Aula
function bookAula(aula, socio) {
	var op = "book-aula";
	var data = data;
	var aula = aula;
	var socio = socio;
	
	$.post("../includes/myhut.php", { 
		data: data,
		aula: aula,
		socio: socio,
		op: op
		}, 
			
	function(data) {
		if(data == 1) {
			$.mobile.changePage( "info.php?op=3", { role: "dialog" });	
			loadAulasReservadas();
		}
		else if(data == -1)
			$.mobile.changePage( "info.php?op=4", { role: "dialog" });
		else if(data == -2)
			$.mobile.changePage( "info.php?op=11", { role: "dialog" });
		else if(data == -3)
			$.mobile.changePage( "info.php?op=10", { role: "dialog" });
		else
			$.mobile.changePage( "info.php?op=5&i="+escape(data), { role: "dialog" });
	});
			
	return false;
}
*/

    }   
     
    public static void bookClassThreadHut(String user, String pass, String classId, Date classDate) throws Exception {
        System.out.println("Starting a new booking thread");

        // 1 - waits until time to book class
        // 2 - starts checking for class availability
        // 3 - Book class when available to be booked.
        // getClassAvailabilityHUT(String classId); // obtain userId from this function
        // bookClassHUT(String classId, String userId, Date classDate);
        Date currentDate = new Date();
        long timeSleep = 0;
        long timeDiff = 0;
        String phpCookie = "";
        String userId = "";

        timeDiff = (classDate.getTime() - currentDate.getTime()) / 1000;

        Date tempDate = new Date(timeDiff * 1000);

        if (timeDiff < 0) {
            System.out.println("This class already started and cannot be booked");
            JOptionPane.showMessageDialog(null, "The class: " + classId + " already started and cannot be booked");
            return;
        } else if (timeDiff < bookClassPeriod) {
            System.out.println("Less than 10 hours until class, trying to book immediatly");
            //JOptionPane.showMessageDialog(null, "The class: " + classId + " is in less than 10 hours , trying to book immediatly" );
        } else if (timeDiff < maxWaitTime) {
            timeSleep = (timeDiff - bookClassPeriod - beforeWaitTime);
            System.out.println("More that 10 hours before class, going to sleep for " + timeSleep + " seconds");
            JOptionPane.showMessageDialog(null, "The class: " + classId + " is in more than 10 hours, going to sleep for " + timeSleep + " seconds");

            if (timeDiff > beforeWaitTime) {
                try {
                    Thread.sleep(timeSleep * 1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    System.out.println("Returning1...");
                    return;
                }
            }
        } else {
            System.out.println("Not possible to book, more that 7 days until class");
            JOptionPane.showMessageDialog(null, "The class: " + classId + " is more that 7 days ahead, not possible to book,");
            return;
        }

        do {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                System.out.println("Returning2...");
                return;
            }

            try {
                userId = getClassAvailabilityHUT(/*phpCookie,*/ classId);
            } catch (Exception ex) {
                // Catch connection exception and continue program execution
                System.out.println(ex.getMessage());
                System.out.println("Connection issue to FitnessHut server, continuing program execution in 5 seconds");
            }
            currentDate = new Date();
            timeDiff = (classDate.getTime() - currentDate.getTime()) / 1000;

        } while (userId == "Unavailable" && timeDiff > 0);

        if (userId != "Unavailable") {
            if (bookClassHUT(phpCookie, classId, userId, classDate)) {
                JOptionPane.showMessageDialog(null, "Class: " + classId + " has been booked successfully!");
            } else {
                JOptionPane.showMessageDialog(null, "Class: " + classId + " could not be booked!");
            }
        }

        System.out.println("Ending a booking thread");
    }
    
    private static void sendFromGMail(String from, String pass, String[] to, String subject, String body) {
        Properties props = System.getProperties();
        String host = "smtp.gmail.com";
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.user", from);
        props.put("mail.smtp.password", pass);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(from));
            InternetAddress[] toAddress = new InternetAddress[to.length];

            // To get the array of addresses
            for( int i = 0; i < to.length; i++ ) {
                toAddress[i] = new InternetAddress(to[i]);
            }

            for( int i = 0; i < toAddress.length; i++) {
                message.addRecipient(Message.RecipientType.TO, toAddress[i]);
            }

            message.setSubject(subject);
            message.setText(body);
            Transport transport = session.getTransport("smtp");
            transport.connect(host, from, pass);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        }
        catch (AddressException ae) {
            ae.printStackTrace();
        }
        catch (MessagingException me) {
            me.printStackTrace();
        }
    }

    
}
    
        
