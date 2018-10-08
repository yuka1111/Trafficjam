import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Randomcatch {
    /**

     * RandomTableからseedc番目の乱数を取り出し返す。

     * @return

     */
    public static long getSeedFormRandomTable(int seedc) {

            String stringseedc= "" + seedc;

            try {
            		File file = new File("src/Random.table100");
                    BufferedReader br= new BufferedReader(new FileReader(file));
                    while(true) {
                            String s= br.readLine();

                            if(s == null) {
                                    br.close(); return(0);
                            }
                            String[] breakstring= s.split("[ :,\t\n]");

                            if(breakstring.length == 2) {

                                    if(stringseedc.equals(breakstring[0])) {
                                            br.close();

return(Long.parseLong(breakstring[1]));
                                    }
                            }
                    }
            } catch (FileNotFoundException e) {
                    e.printStackTrace();
            } catch (IOException e) {
                    e.printStackTrace();
            }
            throw new Error();
    }

    public static void main(String[] args) {
		int i=Integer.parseInt(args[0]);
		System.out.println(getSeedFormRandomTable(i));
	}
}
