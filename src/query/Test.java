package query;

public class Test {
    public static void main(String[] args) throws Exception {
        String alex= "failure method";
        int counter=1;
        for(String s : new QuerySearcher(alex).rankQueryDoc()){
            System.out.println(counter + ":" + s);
            counter+=1;
        }
    }
}
