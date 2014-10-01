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

/**
 *
 * @author Rui Trigo
 */
public class FitnessHutBooking {
   
    private static final String USER_AGENT = "Mozilla/5.0";
    
    //private static final String loginURL = "http://m.fitnesshut.pt/includes/login.php"; //POST
    private static final String loginURL = "http://www.fitnesshut.pt/myhut/login.php"; //POST
    
    //private static final String bookClassURL = "http://m.fitnesshut.pt/includes/myhut.php"; //POST
    private static final String bookClassURL = "http://www.fitnesshut.pt/myhut/pages/myhut.php"; //POST
    
    //private static final String getClassAvailabilityURL = "http://m.fitnesshut.pt/pages/aula.php?id="; //GET
    private static final String getClassAvailabilityURL = "http://www.fitnesshut.pt/myhut/pages/get-aula.php?id="; //GET
    
    //private static final String getClassesURL = "http://m.fitnesshut.pt/pages/get-aulas.php?id="; //GET
    private static final String getClassesURL = "http://www.fitnesshut.pt/myhut/pages/clube-aulas.php?id="; //GET
    
    //GET http://m.fitnesshut.pt/pages/get-aulas.php?id=5&date=2014-03-20 HTTP/1.1
    // Buscar aulas de Odivelas do proprio dia http://m.fitnesshut.pt/pages/aulas.php?id=5 //GET
    // Buscar aulas de Odivelas do dia especificado http://m.fitnesshut.pt/pages/get-aulas.php?id=5&date=2014-03-20

    public static final long maxWaitTime = 604800; //604800 seconds is 7 days
    public static final long beforeWaitTime = 60; //60 is in secs
    public static final long bookClassPeriod = 36000; //36000 seconds is 10 hours
    
    public static final String amoreirasHUT = "1";
    public static final String cascaisHUT = "2";
    public static final String trindadeHUT = "3";
    public static final String arcodocegoHUT = "4";
    public static final String odivelasHUT = "5";
    public static final String bragaHUT = "6";
    public static final String picoasHUT = "7";
        
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
    
    
    public static String loginHUT(String user, String pass) throws Exception{
        
        String loginResult = "AuthFailed";
        
        String url = loginURL;
        
        //String urlParameters = "email=" + user + "&password=" + pass;
        String urlParameters = "myhutemail=" + user + "&myhutpassword=" + pass;
        
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
            //System.out.println("Content-Type: " + con.getContentType());
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
            loginResult = con.getHeaderField("Set-Cookie");
        }
        //print result
        //System.out.println(response.toString());
        System.out.println(loginResult);

        return loginResult;
    }
    
    public static String getClassAvailabilityHUT(String phpCookie, String classId) throws Exception{

        String userId = "Unavailable" ;
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
            //boolean isClassSoldOut = data.contains("Esgotado");
            boolean isClassSoldOut = data.contains("esgotado");
            
            //boolean isClassAvailable = data.contains("Disponível");
            boolean isClassAvailable = data.contains("disponivel");
            
            boolean isClassBookable = data.contains("bookAula");

            System.out.println("isClassSoldOut: " + isClassSoldOut);
            System.out.println("isClassAvailable: " + isClassAvailable);
            System.out.println("isClassBookable: " + isClassBookable);
            System.out.println(userId);
            
            //System.out.println(response.toString());
            
            /*
            JOptionPane.showMessageDialog(null, "Class: " + classId + " is:\n\r" + 
                                                "Available: " + isClassAvailable +
                                                "\n\rSold Out: " + isClassSoldOut +
                                                "\n\rBookablee: " + isClassBookable);
            */
            if (isClassAvailable & isClassBookable){
                Document doc = Jsoup.parse(response.toString());
                Element e = doc.getElementById("b-book" + classId);
                userId = e.attr("onclick");
                userId = userId.substring( userId.indexOf(",") + 1 , userId.lastIndexOf(")"));
            }
            
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

            BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
            }
            in.close();

            System.out.println(response.toString());

            Document doc = Jsoup.parse(response.toString());
            
            //Element classList = doc.getElementById("aulas-list");
            Elements links = doc.getElementsByClass("aulas-content-menu-aula");
            
            for (Element link : links) {
                String classId = link.attr("onclick");
                String classInfo = classId.substring( classId.indexOf("\"") + 1 , classId.lastIndexOf("\""));
                classId = classId.substring( classId.indexOf("(") + 1 , classId.lastIndexOf(","));
                 
                Element e = link.child(0);

                String classTimeDuration = e.childNode(0).toString();
                String classTime = classTimeDuration.substring( 0 , classTimeDuration.indexOf("/") - 1 );
                String classDuration = classTimeDuration.substring( classTimeDuration.indexOf("/") + 2 );
                String classLocation = e.childNode(2).toString();

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
            
 
            /*
            
            Elements links = doc.getElementsByTag("li");
            
            for (Element link : links) {
                Element e = link.child(0);
                String classId = e.attr("onclick");
                classId = classId.substring( classId.indexOf("(") + 1 , classId.lastIndexOf(")"));
                String classTime = e.child(0).child(0).text();
                String classInfo = e.child(1).child(0).text();
                String classDurationLocation = e.child(1).child(1).text();
                String classDuration = classDurationLocation.substring( 0 , classDurationLocation.indexOf(",") );
                String classLocation = classDurationLocation.substring( classDurationLocation.indexOf(",") + 1 );


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
            */
            return true;
    }

    public static boolean bookClassHUT(String phpCookie, String classId, String userId, Date classDate) throws Exception{
   
        
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String url = bookClassURL ;
        //String urlParameters = "data=" + df.format(classDate) + "&aula=" + classId + "&socio=" + userId + "&op=book-aula" ;
        String urlParameters = "aula=" + classId + "&socio=" + userId + "&op=book-aulas" ;        
                
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("Cookie", phpCookie);

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
      
        if ( "  1".equals(response.toString()) ) {
                    System.out.println("Aula marcada com sucesso");
                    return true;
        }
        System.out.println("Nao foi possivel marcar a aula, codigo de erro: " + response.toString());
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
        
        timeDiff = ( classDate.getTime() - currentDate.getTime() ) / 1000;
        
        Date tempDate = new Date(timeDiff*1000);
        
        if ( timeDiff < 0 ){
            System.out.println("This class already started and cannot be booked");
            JOptionPane.showMessageDialog(null, "The class: " + classId + " already started and cannot be booked" );
            return;
        }
        else if( timeDiff < bookClassPeriod){
            System.out.println("Less than 10 hours until class, trying to book immediatly");
            //JOptionPane.showMessageDialog(null, "The class: " + classId + " is in less than 10 hours , trying to book immediatly" );
        }
        else if( timeDiff < maxWaitTime ){
            timeSleep = (timeDiff - bookClassPeriod - beforeWaitTime) ;
            System.out.println("More that 10 hours before class, going to sleep for " + timeSleep + " seconds");
            JOptionPane.showMessageDialog(null, "The class: " + classId + " is in more than 10 hours, going to sleep for " + timeSleep + " seconds");
            
            if ( timeDiff > beforeWaitTime){
                try {
                    Thread.sleep( timeSleep * 1000);
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    System.out.println("Returning1...");
                    return;
                }
            }
        }
        else{
            System.out.println("Not possible to book, more that 7 days until class");
            JOptionPane.showMessageDialog(null, "The class: " + classId + " is more that 7 days ahead, not possible to book,");
            return;
        }
            
         

        try {
            phpCookie = loginHUT(user, pass);

            if (phpCookie != "AuthFailed"){
                
                do{    
                    
                    try {
                        Thread.sleep(5000);
                    } catch(InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        System.out.println("Returning2...");
                        return;
                    }
                    
                    userId = getClassAvailabilityHUT(phpCookie, classId);
                    currentDate = new Date();
                    timeDiff = ( classDate.getTime() - currentDate.getTime() ) / 1000;
                    
                }
                while (userId == "Unavailable" && timeDiff > 0);
                
                if( userId != "Unavailable" ){
                    if ( bookClassHUT(phpCookie, classId, userId, classDate) ){
                        JOptionPane.showMessageDialog(null, "Class: " + classId + " has been booked successfully!" );
                    }
                    else{
                        JOptionPane.showMessageDialog(null, "Class: " + classId + " could not be booked!" );
                    }
                }
                
            }
            else{
                JOptionPane.showMessageDialog(null, "Authentication failed during the class booking process!" );
            }


        } catch (Exception ex) {
            Logger.getLogger(FitnessHutBookingGUI.class.getName()).log(Level.SEVERE, null, ex);
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
    
        
