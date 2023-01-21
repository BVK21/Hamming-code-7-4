import com.google.common.base.Splitter;
import com.google.common.collect.Lists;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HammingCode {

    public static double threshold ;
    public static String fullSignalFilePath = "/Users/bhim/IdeaProjects/Hamming-code-7-4/src/main/resources/fullSignalFile1";



    public static void main(String[] args) throws Exception {
        mergeSignal("/Users/bhim/IdeaProjects/Hamming-code-7-4/src/main/resources/proj1_testsignal1","/Users/bhim/IdeaProjects/Hamming-code-7-4/src/main/resources/proj2_noisesignal1");
        List<List<Double>> partitionedList = readSignalFromFile("/Users/bhim/IdeaProjects/Hamming-code-7-4/src/main/resources/fullSignalFile1");
        System.out.println(getCleanedBitsString(getBitsString(partitionedList,threshold)));
    }


    public  static void mergeSignal(String signalFile, String noiseFile) throws Exception {
        if (signalFile == null || signalFile.isEmpty() || noiseFile == null || noiseFile.isEmpty())
            throw new Exception("Invalid Input");

        BufferedReader signal = new BufferedReader(new FileReader(signalFile));
        BufferedReader noise = new BufferedReader(new FileReader(noiseFile));
        String signalString;
        String noiseString;
        List<Double> dataList = new ArrayList<>();
        while (((signalString = signal.readLine()) != null) && ((noiseString = noise.readLine()) != null)) {
            dataList.add(Double.parseDouble(signalString) + Double.parseDouble(noiseString));
        }

        FileWriter writer = new FileWriter(fullSignalFilePath);
        for(Double d: dataList) {
            writer.write(d + System.lineSeparator());
        }
        writer.close();



    }


    public static List<List<Double>> readSignalFromFile(String filePath) throws Exception {
        if (filePath == null || filePath.isEmpty())
            throw new Exception("Invalid Input");
        BufferedReader in = new BufferedReader(new FileReader(filePath));
        String str;

        List<Double> list = new ArrayList<>();
        while ((str = in.readLine()) != null) {
            list.add(Double.parseDouble(str));
        }

        List<Double> sampleData = new ArrayList<>();
        for(int i = 0 ; i < 1000000 ; i ++){
            sampleData.add(list.get(i));
        }
        threshold = getThreshold(sampleData);
        int startIndex = 0 ;
        for(int i = 0 ; i < list.size() ; i++){
            if(list.get(i) > threshold){
                startIndex = i;
                break;
            }
        }
        startIndex = startIndex - 5;
        list = list.subList(startIndex, list.size());
        List<List<Double>> partitionedList = Lists.partition(list, 100);
        return partitionedList;
    }

    public static double getThreshold(List<Double> signalData) throws Exception {
        if(signalData == null || signalData.size() != 1000000)
            throw new Exception("Invalid Parameter Passed !!");
        double sum = 0 ;
        double mean = 0 ;
        double standardDeviation  = 0 ;
        for(Double data : signalData){
            sum+=data;
        }
        mean = sum/signalData.size();

        for(Double data: signalData) {
            standardDeviation += Math.pow(data - mean, 2);
        }
        standardDeviation = Math.sqrt(standardDeviation/signalData.size());
        return (mean * 8 + standardDeviation * 16);


    }

    public static String getBitsString(List<List<Double>> partitionedSignalData , double threshold) throws Exception {
        try {
            if (partitionedSignalData == null || partitionedSignalData.size() == 0)
                throw new Exception("Invalid Parameter");
            StringBuilder sb = new StringBuilder();

            for (List<Double> data : partitionedSignalData) {
                System.out.println(data.get(0));
                System.out.println(data.get(data.size() - 1));
                if (data.size() == 100) {
                    boolean bitFound = false;
                    for (int i = 0; i < 20; i++) {
                        if (data.get(i) > threshold) {
                            sb.append("0");
                            bitFound = true;
                            break;
                        }
                    }
                    if (!bitFound) {
                        for (int i = 20; i < 100; i++) {
                            if (data.get(i) > threshold) {
                                sb.append("1");
                                bitFound = true;
                                break;
                            }
                        }
                    }
                }
            }
            sb.delete(0,8);
            return sb.toString();
        }catch (Exception e){
            throw new Exception("Exception");
        }
    }

    public static StringBuilder getCleanedBitsString(String uncleanedBits) throws Exception {
        try {

            if (uncleanedBits == null || uncleanedBits.isEmpty()) {
                throw new Exception("Invalid Parameter ");
            }
            StringBuilder sb = new StringBuilder();
            Iterable<String> chunks = Splitter.fixedLength(7).split(uncleanedBits);
            for (String s : chunks) {
                sb.append(decode(s));
                
                }

            System.out.println(new String(binaryToBytes(sb.toString()),StandardCharsets.UTF_8));
            return sb;
        }catch (Exception e){
            throw new Exception("Exception");
        }
    }

    public static String binaryToText(String binary) {
        return Arrays.stream(binary.split("(?<=\\G.{8})"))/* regex to split the bits array by 8*/
                .parallel()
                .map(eightBits -> (char)Integer.parseInt(eightBits, 2))
                .collect(
                        StringBuilder::new,
                        StringBuilder::append,
                        StringBuilder::append
                ).toString();
    }

    public static byte[] binaryToBytes(String input) {
        byte[] ret = new byte[input.length() / 8];
        for (int i = 0; i < ret.length; i++) {
            String chunk = input.substring(i * 8, i * 8 + 8);
            ret[i] = (byte) Short.parseShort(chunk, 2);
        }
        return ret;
    }






    public static String decode (String bits) throws Exception {
        if(bits.length() < 7)
            throw new Exception("Invalid Parameter Passed ");
        int[] parity = new int[3];
        int[] actualParity = new int[3];
        int[] fullData = new int[7];
        int[] data = new int[4];
        int pIndex = 0 ;
        boolean syndrome = false;
        for(int i = 4 ; i < 7 ; i ++){
            parity[pIndex++] = Character.getNumericValue(bits.charAt(i));

        }
        for(int i = 0 ; i < bits.length() ; i++){
            fullData[i] = Character.getNumericValue(bits.charAt(i));
        }
        for(int j = 0 ; j < 4 ; j++){
            data[j] = Character.getNumericValue(bits.charAt(j));
        }

        actualParity[0] = data[0] ^ data [1] ^ data[2];
        actualParity[1] = data[1] ^ data [2] ^ data[3];
        actualParity[2] = data[0] ^ data [1] ^ data[3];

        syndrome = Arrays.equals(parity,actualParity);
       return checkSyndrome(fullData);

    }

    public static String checkSyndrome(int[] fullData){
        String binaryStr = new String();
        int[] result = {0,0,0};
        int[] temporary = new int[fullData.length];
        for(int i = 0 ; i < fullData.length ; i++){
            temporary[i] = fullData[i];
        }
        int[][] h = {{1,0,1},{1,1,1},{1,1,0},{0,1,1},{1,0,0},{0,1,0},{0,0,1}};
        int[] newArray = new int[h[0].length]; // create the array that will contain the result of the array multiplication
        assert h.length >0 && h.length == fullData.length;
        for (int i = 0; i < h[0].length; i++) { // use nested loops to multiply the two arrays together
            int c = 0;
            for (int j = 0; j < fullData.length; j++) {
                int l = fullData[j];
                int m = h[j][i];
                c ^= l * m; // sum the products of each set of elements
            }
            newArray[i] = c;

        }
        if(!Arrays.equals(newArray,result)){
            int in = linearCheck(h,newArray);
            if(fullData[in -1] == 0){
                fullData[in -1] = 1;
            }else{
                fullData[in - 1] =  0;
            }
            return intToString(fullData).substring(0,4);

        }
       else {

            return intToString(fullData).substring(0,4);
        }
    }




    static int binaryToInt (String binary){
        char []cA = binary.toCharArray();
        int result = 0;
        for (int i = cA.length-1;i>=0;i--){
            //111 , length = 3, i = 2, 2^(3-3) + 2^(3-2)
            //                    0           1
            if(cA[i]=='1') result+=Math.pow(2, cA.length-i-1);
        }
        return result;
    }

    static int linearCheck(int ar[][], int arr[])
    {
        for (int i = 0; i < ar.length; i++)
        {
            boolean matched = true;

            for (int j = 0; j < arr.length; j++)
            {
                if (ar[i][j] != arr[j])
                {
                    matched = false;
                    break;
                }
            }
            if (matched) {
                return i + 1;
            }
        }
        return -1;
    }


    public static String intToString(int[] full){
        String str = new String();
        for(int i = 0 ; i < full.length ; i++){
            str = str + full[i];
        }
        return str;
    }
    }







