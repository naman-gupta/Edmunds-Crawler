import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;

public class MySQLAccess {

	String url = "jdbc:mysql://localhost/";
	String dbName = "****";
	String driver = "com.mysql.jdbc.Driver";
	String userName = "root";
	String password = "****";

	public Connection getMySqlConnection() throws Exception {
		Class.forName(driver);

		Connection connection = DriverManager.getConnection(url + dbName,
				userName, password);
		System.out.println(connection);
		return connection;

	}

	public void loadReview(String fileName) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		String line;
		Connection con = getMySqlConnection();
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
		PreparedStatement ps = null;
		int count = 1;
		while ((line = reader.readLine()) != null) {

			ps = con.prepareStatement("insert into Reviews(rdate,vehicle,rtitle,review) values(?,?,?,?)");

			String[] data = line.split("\t");

			String[] dateSplit = data[0].split("/");
			String dateFormatted = dateSplit[0] + "/" + dateSplit[1] + "/20"
					+ dateSplit[2];

			java.util.Date date = formatter.parse(dateFormatted);
			java.sql.Date sqlStartDate = new java.sql.Date(date.getTime());

			ps.setDate(1, sqlStartDate);
			ps.setString(2, data[2]);
			ps.setString(3, data[1]);
			ps.setString(4, data[3]);

			ps.executeUpdate();
			System.out.println(count++);
		}

		if (ps != null)
			ps.close();
		if (con != null)
			con.close();

	}

	public void populateReviewSentences() throws Exception {
		Connection con = getMySqlConnection();
		PreparedStatement ps = con
				.prepareStatement("select rid,rtitle,review from Reviews");
		ResultSet rs = ps.executeQuery();

		while (rs.next()) {
			int rid = rs.getInt("rid");
			String review = rs.getString("review");
			String title = rs.getString("rtitle");
			String reviews = title + ". " + review;
			Connection conn1 = getMySqlConnection();

			PreparedStatement ps1 = conn1
					.prepareStatement("insert into Review_Sentences(rid,sen_id,sentence) values(?,?,?)");

			int count = 1;

			List<String> ls = getSentences(reviews);
			for (String sentence : ls) {
				ps1.setInt(1, rid);
				ps1.setInt(2, count);
				ps1.setString(3, sentence);
				ps1.executeUpdate();
				count++;
			}

		}
		if (ps != null)
			ps.close();

		if (rs != null)
			rs.close();

		if (con != null)
			con.close();
	}

	public List<String> getSentences(String paragraph) {
		Reader reader = new StringReader(paragraph);
		DocumentPreprocessor dp = new DocumentPreprocessor(reader);

		List<String> sentenceList = new LinkedList<String>();
		Iterator<List<HasWord>> it = dp.iterator();
		while (it.hasNext()) {
			StringBuilder sentenceSb = new StringBuilder();
			List<HasWord> sentence = it.next();
			for (HasWord token : sentence) {
				if (sentenceSb.length() > 1) {
					sentenceSb.append(" ");
				}
				sentenceSb.append(token);
			}
			sentenceList.add(sentenceSb.toString());
		}

		return sentenceList;

	}

	public static void main(String[] args) throws Exception {
		MySQLAccess ms = new MySQLAccess();
		ms.loadReview("data/Reviews/NissanAltima.txt");
		ms.populateReviewSentences();
	}
}
