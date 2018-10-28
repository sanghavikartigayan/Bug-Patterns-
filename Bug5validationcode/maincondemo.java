
public class maincondemo {

	public static void main(String[] args) throws ClassNotFoundException {

		try {
			System.out.println("****************This is Try Block****************");
			System.out.println("Try1");
			try {
				System.out.println("****************This is Try Block****************");
				System.out.println("Try2");
			} catch (ArithmeticException e) {

				System.out.println("Catch----1");
			}

			catch (Exception e) {

				System.out.println("Catch----1");

			}
		} catch (ArithmeticException e) {
			System.out.println("****************This is catch Block****************");
			System.out.println("Catch----3");

		} catch (NullPointerException e) {
			System.out.println("****************This is catch Block****************");
			System.out.println("Catch----3");
		} finally {
			try {
				System.out.println("Try3 inside Finally1");
			} catch (NullPointerException e) {
				System.out.println("Catch----4");
			} catch (Exception e) {
				System.out.println("Catch----4");
			}
		} /*
			 * finally {
			 * System.out.println("****************This is Finallly Block****************");
			 * System.out.println("Finally 2 with try1"); }
			 */

	}

}
