public class Review {

	private String title;
	private String vehicle;
	private String review;
	private String rdate;

	public Review(String title, String vehicle, String review, String rdate) {
		this.title = title;
		this.vehicle = vehicle;
		this.review = review;
		this.rdate = rdate;
	}

	public String toString() {
		StringBuffer data = new StringBuffer("");
		data.append(rdate);
		data.append("\t");

		data.append(title);
		data.append("\t");

		data.append(vehicle);
		data.append("\t");

		data.append(review);

		return data.toString();
	}

}
