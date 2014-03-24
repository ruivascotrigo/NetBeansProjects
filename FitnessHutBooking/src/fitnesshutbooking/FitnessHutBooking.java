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
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Rui Trigo
 */
public class FitnessHutBooking {
   
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String loginURL = "http://m.fitnesshut.pt/includes/login.php"; //POST
    private static final String bookClassURL = "http://m.fitnesshut.pt/includes/myhut.php"; //POST
    private static final String getClassAvailabilityURL = "http://m.fitnesshut.pt/pages/aula.php?id="; //GET
    private static final String getClassesURL = "http://m.fitnesshut.pt/pages/get-aulas.php?id="; //GET
    //GET http://m.fitnesshut.pt/pages/get-aulas.php?id=5&date=2014-03-20 HTTP/1.1
    // Buscar aulas de Odivelas do proprio dia http://m.fitnesshut.pt/pages/aulas.php?id=5 //GET
    // Buscar aulas de Odivelas do dia especificado http://m.fitnesshut.pt/pages/get-aulas.php?id=5&date=2014-03-20

    public static final long maxWaitTime = 604800; //604800 seconds is 7 days
    public static final long beforeWaitTime = 60; //60 is in secs
    public static final long bookClassPeriod = 36000; //36000 seconds is 10 hours
    
    public static final String odivelasHUT = "5";
    
    public Vector<Vector> todayClasses = new Vector<Vector>();
    public Vector<Vector> tomorrowClasses = new Vector<Vector>();
    
    //private String phpCookie = "";
    private String userId = "";
    
    
    
    
    public static String loginHUT(String user, String pass) throws Exception{
        
        String loginResult = "AuthFailed";
        
        String url = loginURL;
        String urlParameters = "email=" + user + "&password=" + pass;
        
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
            loginResult = con.getHeaderField("Set-Cookie");
        }
        //print result
        System.out.println(response.toString());
        System.out.println(loginResult);

        return loginResult;
    }
    
    public static boolean getClassAvailabilityHUT(String phpCookie, String classId) throws Exception{

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
            
            Document doc = Jsoup.parse(response.toString());
            Element e = doc.getElementById("b-book" + classId);
            
            String userId = e.attr("onclick");
            userId = userId.substring( classId.indexOf(",") + 1 , classId.lastIndexOf(")"));
            

            
            System.out.println(isClassSoldOut);
            System.out.println(isClassAvailable);
            System.out.println(isClassBookable);
            System.out.println(userId);
            
            System.out.println(response.toString());
            
            JOptionPane.showMessageDialog(null, "Class: " + classId + " is:\n\r" + 
                                                "Available: " + isClassAvailable +
                                                "\n\rSold Out: " + isClassSoldOut +
                                                "\n\rBookablee: " + isClassBookable);
            
            return (isClassAvailable & isClassBookable);
            //print result
            //System.out.println(response.toString());
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

            return true;
    }

    public static boolean bookClassHUT(String phpCookie, String classId, String userId, Date classDate) throws Exception{
   
        
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String url = bookClassURL ;
        String urlParameters = "data=" + df.format(classDate) + "&aula=" + classId + "&socio=" + userId + "&op=book-aula" ;
                
                
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
     
    public static void bookClassThreadHut(String classId, Date classDate) throws Exception {
        System.out.println("My Static Method");
        
        // 1 - waits until time to book class
        // 2 - starts checking for class availability
        // 3 - Book class when available to be booked.
                
        // getClassAvailabilityHUT(String classId); // obtain userId from this function
        // bookClassHUT(String classId, String userId, Date classDate);
        
        Date currentDate = new Date();
        long secsToWait = maxWaitTime; //604800 is 7 days
        long timeDiff = 0;
        /*            
        timeDiff = ( classDate.getTime() - currentDate.getTime() ) / 1000 - this.instanceOfFitnessHutBooking.bookClassPeriod;
        if (timeDiff > 0 && timeDiff < secsToWait ){
            secsToWait = timeDiff;
        }

        if ( secsToWait == this.instanceOfFitnessHutBooking.maxWaitTime){
            System.out.println("No classes to book");
            return; //no classes to book
        }


        try {
            //JOptionPane.showMessageDialog(null, "Booking class: " + classId + " at " + dia);

            this.instanceOfFitnessHutBooking.getClassAvailabilityHUT(classId);


        } catch (Exception ex) {
            Logger.getLogger(FitnessHutBookingGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        */
    }
    
    
    
}
    
        
