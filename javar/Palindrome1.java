import java.util.Scanner;

public class Palindrome1 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("enter sentence: ");
        String s = sc.nextLine();

        String revs = "";
        for (int i = s.length() - 1; i >= 0; i--) {
            revs = revs + s.charAt(i);
        }

        if (s.equals(revs)) {
            System.out.println("palindrome");
        } else {
            System.out.println("not palindrome");
        }

        sc.close();
    }
}