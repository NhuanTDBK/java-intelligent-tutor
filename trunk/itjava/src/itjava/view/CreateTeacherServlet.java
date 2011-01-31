package itjava.view;

import itjava.model.ResultEntry;
import itjava.db.*;

import java.io.IOException;
import java.util.ArrayList;
import java.sql.*;
import java.security.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 
 */
@WebServlet(description = "Creates a new Teacher", urlPatterns = { "/CreateTeacherServlet" })
public class CreateTeacherServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	ArrayList<ResultEntry> sourceCodes;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateTeacherServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Connection conn = null;
		PreparedStatement pst = null;
		PreparedStatement ucpst = null;
		ResultSet rs = null;

	   try
	   {
		   
		   String firstName = request.getParameter("firstName");
		   String lastName = request.getParameter("lastName");
		   String school = request.getParameter("school");
		   String email = request.getParameter("email");
		   String username = request.getParameter("username");
		   String password = request.getParameter("password");
			byte[] defaultBytes = password.getBytes();
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(defaultBytes);
			byte messageDigest[] = algorithm.digest();
					
			StringBuffer hexString = new StringBuffer();
			for (int i=0;i<messageDigest.length;i++) {
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
			}
			messageDigest.toString();
			password=hexString+"";

		    conn = DBConnection.GetConnection();
			String usercheck = "SELECT username, email FROM teachers WHERE username = ? OR email = ?";
		   ucpst = conn.prepareStatement(usercheck);
		   ucpst.setString(1, username);
		   ucpst.setString(2, email);
		   rs = ucpst.executeQuery();
		   if(!rs.next()){
				String sql = "INSERT INTO teachers(username, password, firstName, lastName, school, email) values(?,?,?,?,?,?)";
				pst = conn.prepareStatement(sql);
				pst.setString(1, username);
				pst.setString(2, password);
				pst.setString(3, firstName);
				pst.setString(4, lastName);
				pst.setString(5, school);
				pst.setString(6, email);
				pst.executeUpdate();
				sql = "SELECT teacherID, username FROM teachers WHERE username = ?";
				pst = conn.prepareStatement(sql);
				pst.setString(1, username);
				rs = pst.executeQuery();
				rs.next();
				HttpSession session = request.getSession(true);
				session.setAttribute("userName", rs.getString("username"));
				session.setAttribute("userID", rs.getString("teacherID"));
				session.setAttribute("userLevel", "teacher");
				conn.close();
				String redirectURL1 = "teachers.jsp"; 
				response.sendRedirect(redirectURL1);
		   }else{
			   if(rs.getString("email").equals(email) && rs.getString("username").equals(username)){
				   conn.close();
				   String redirectURL2 = "teachers.jsp?error=3"; 
				   response.sendRedirect(redirectURL2);
			   }else if(rs.getString("username").equals(username)){
				   conn.close();
				   String redirectURL3 = "teachers.jsp?error=1"; 
				   response.sendRedirect(redirectURL3);
			   }else{
				   conn.close();
				   String redirectURL4 = "teachers.jsp?error=2"; 
				   response.sendRedirect(redirectURL4); 
			   }
		   }
	   }
	   catch(Exception e) {
  	     e.printStackTrace();
  	   }
  	   finally {
  		 try{
			   conn.close(); 
		   }catch(Exception e){
			 e.printStackTrace();
		   }
  	   }
		
	}

}
