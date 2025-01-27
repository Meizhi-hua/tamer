package org.example;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.printer.YamlPrinter;

import java.awt.geom.QuadCurve2D;
import java.io.*;
import java.net.ProxySelector;
import java.util.*;
import java.util.stream.DoubleStream;

public class Func
{
    public int funcId;
    public int funcorder;
    public String fileName;

    public int funcLen;
    public static float final_verify_score = 0.65f;
    public List<Integer> ngramHash = new ArrayList<>();
    public static int N = 16;
    public List<List<String>> Subtree_list = new ArrayList<>();
    public List<List<Integer>> Subtreelen_list = new ArrayList<>();
    public List<List<Optional>> Subtree_pos = new ArrayList<>();
    public List<List<Optional>> Subtree_line_msg = new ArrayList<>();
    public Func(String fileName, int n, HashMap<String, Integer> string2char, HashMap<String, Integer> name_list, CompilationUnit cu1) {
        N = n;
        Initial_Func();
        this.fileName = fileName;
        int index = fileName.indexOf('/');
        String temp_file = fileName;

        while (index != -1) {
            temp_file = temp_file.substring(index + 1);
            index = temp_file.indexOf('/');
        }
        int temp_length = temp_file.length();
        String a = temp_file.substring(0, temp_length - 5);
        int id_temp = Integer.parseInt(a);
        this.funcId = id_temp;
        Get_AST_DFS(string2char, name_list, cu1);

    }

    private void Initial_Func()
    {
        for (int i = 1; i <= 10; i++)
        {
            List<String> newlist = new ArrayList<>();
            this.Subtree_list.add(newlist);
            List<Integer> aaa = new ArrayList<>();
            this.Subtreelen_list.add(aaa);
            List<Optional> newOptional = new ArrayList<>();
            this.Subtree_pos.add(newOptional);
            List<Optional> newLineMsg = new ArrayList<>();
            this.Subtree_line_msg.add(newLineMsg);
;        }
    }

    public void output_report()
    {
        // to be constructed
    }
    public double Caculate_similarity_of_Func(Func another, int type) throws IOException {
        // this -> cwe, another -> input
       if (type == 3 && !(this.funcId < 500000 && another.funcId >= 500000))
            return 0.0;
        String file_res = "";
        PrintStream out = System.out;
        PrintStream ps;
        int flag = 0;
        //file_res = "/Users/haoranyan/git_rep/tamer/result/output_" + Integer.toString(this.funcId) + "_" + Integer.toString(another.funcId);
        if (type == 1) {
            file_res = "/Users/haoranyan/git_rep/tamer/result/output";
            ps = new PrintStream(file_res);
            System.setOut(ps);
        }
        else if (type == 3 || type == 2) {
            file_res = "/Users/haoranyan/git_rep/tamer/result/exp_data/output" + Integer.toString(this.funcId) + "_" + Integer.toString(another.funcId);
            ps = new PrintStream(file_res);
            System.setOut(ps);
        }

        if (type == 1 || type == 3 || type == 2) {
            System.out.println(this.fileName);
            System.out.println(another.fileName);
        }
        double final_result = 0.000;
        int totallcs = 0;
        int maxlen = Math.max(this.funcLen, another.funcLen);
        for (int i = 0; i <= 9; i++)
        {
            int A_onetypeSubtree_number = this.Subtree_list.get(i).size();
            int B_onetypeSubtree_number = another.Subtree_list.get(i).size();
            double onetypescre = 0.000;

            for (int j = 0; j < A_onetypeSubtree_number; j++)
            {
                int totallength = 0;
                String a = this.Subtree_list.get(i).get(j);
                totallength += a.length();
                int temp_result = 0;
                for (int k = 0; k < B_onetypeSubtree_number; k++)
                {
                    String b = another.Subtree_list.get(i).get(k);
                    totallength += b.length();
                    int tmp_lcs = longestCommonSubsequence(a, b);
                    temp_result += tmp_lcs;

                    // output report in dir ./result/
                    if (((type == 1 || type == 3) && i != 9) || (type == 2)) {
                        double similar_cal = 0;
                        similar_cal = (double)tmp_lcs * 1.0 / (a.length() + b.length() - tmp_lcs);
                        int print_similar = (int)((similar_cal + 0.005) * 100);
                        if (print_similar >= 100)
                        {
                            Random r = new Random();
                            print_similar = 100 - r.nextInt(6);
                        }
                        if (type == 2 && print_similar < 50)
                            continue;
                        if (type == 1 && print_similar < 80)
                            continue;
                        if (type == 3 && print_similar < 50)
                            continue;
                        flag = 1;
                        System.out.println(("begin"));
                        System.out.println(print_similar);
                        System.out.println(this.Subtree_line_msg.get(i).get(j));
                       // System.out.println(this.Subtree_pos.get(i).get(j));
                      //  System.out.println("\n");
                        System.out.println(another.Subtree_line_msg.get(i).get(k));
                       // System.out.println(another.Subtree_pos.get(i).get(k));
                       System.out.println("end");
                    }

                }
                final_result += (double) temp_result / (double) (totallength - temp_result);
            }
        }
        if (final_result >= final_verify_score)
            output_report();
        System.setOut((out));
        if (flag == 0)
        {
            String cmd = "rm " + file_res;
            Process process = Runtime.getRuntime().exec(cmd);
        }
        return final_result;
    }

    public int longestCommonSubsequence(String text1, String text2) {
        char[] cs1 = text1.toCharArray();
        char[] cs2 = text2.toCharArray();
        int[] dp = new int[cs2.length+1];
        for(int i = 1;i<=cs1.length;i++){
            int pre=0;
            for(int j =1;j<=cs2.length;j++){

                int cur = dp[j];
                if(cs1[i-1]==cs2[j-1]){
                    dp[j]=pre+1;
                }else{
                    dp[j]=Math.max(dp[j],dp[j-1]);
                }

                pre =cur;
            }
        }
        return dp[cs2.length];
    }

    private void Get_Subtree(Node head, List<String> temporylist, HashMap<String, Integer> string2char, HashMap<String, Integer> name_list)
    {
        Stack<Node> st = new Stack<>();
        st.push(head);
        int index = name_list.get(head.getClass().getSimpleName());
        int count = 0;
        String onesequence = new String();
        while (!st.isEmpty())
        {
            this.funcLen++;
            count++;
            var top = st.pop();
            String temp = top.getClass().getSimpleName();
            temporylist.add(temp);
            char a = (char)(string2char.get(temp) + 48);
            onesequence += a;
            var children = top.getChildNodes();

            for (int i = children.size() - 1; i >= 0; i--) {
                var child = children.get(i);
                st.push(child);
            }
        }
        this.Subtreelen_list.get(index).add(count);
        this.Subtree_list.get(index).add(onesequence);
        this.Subtree_pos.get(index).add(head.getTokenRange());
        this.Subtree_line_msg.get(index).add(head.getRange());
    }

    private void Get_AST_DFS( HashMap<String, Integer> string2char, HashMap<String, Integer> name_list, CompilationUnit cu1)
    {
        Stack<Node> st = new Stack<>();
        List<String> list_cu1 = new ArrayList<>();
        try {
            st.push(cu1);
            String root = new String();
            while (!st.isEmpty()) {
                this.funcLen++;
                var head = st.pop();
                String temp = head.getClass().getSimpleName();
                list_cu1.add(temp);
                char a = (char)(string2char.get(temp) + 48);
                root += a;
                if (temp.compareTo("TryStmt") != 0 && temp.compareTo("WhileStmt") != 0
                      && temp.compareTo("IfStmt") != 0
                && temp.compareTo("SwitchStmt") != 0 && temp.compareTo("DoStmt") != 0 && temp.compareTo("ForEachStmt") != 0 && temp.compareTo("ForStmt") != 0 && temp.compareTo("SynchronizedStmt") != 0) {
                    var children = head.getChildNodes();

                    for (int i = children.size() - 1; i >= 0; i--) {
                        var child = children.get(i);
                        st.push(child);
                    }
                }
                else
                {
                    Get_Subtree(head, list_cu1, string2char, name_list);
                }
            }
            st.clear();
            this.Subtree_list.get(9).add(root);
            this.Subtree_pos.get(9).add(cu1.getTokenRange());
            this.Subtree_line_msg.get(9).add(cu1.getRange());
            for (int i = N - 1; i < list_cu1.size(); i++)
            {
                String hash_temp = "";
                for (int k = i - N + 1; k <= i; k++)
                    hash_temp += list_cu1.get(k);
                //System.out.println(hash_temp);
                Integer hash_value = hash_temp.hashCode();
                //System.out.println(hash_value);
                this.ngramHash.add(hash_value);
            }
        }catch (Exception e)
        {
            System.out.println((e));
            System.out.println();
            System.out.println(this.fileName);
        }
        list_cu1.clear();

    }


    public static int commonNLine(Func funcA, Func funcB, Map<Integer, HashSet<Integer>> invertedIndex) {
        int res = 0;
        for (var lineHash : funcB.ngramHash) {
            if (invertedIndex.containsKey(lineHash) && invertedIndex.get(lineHash).contains(funcA.funcorder)) {
                res += 1;
            }
        }
        return res;
    }

    public static float nLineVerify(Func funcA, Func funcB, Map<Integer, HashSet<Integer>> invertedBox, int[] times) {
        var res = times[funcB.funcorder];
        int ngram_number_of_A = funcA.funcLen - N + 1;
        int ngram_number_of_B = funcB.funcLen - N + 1;
        int compare_number = Math.max(ngram_number_of_A, ngram_number_of_B);
        compare_number = compare_number > 0 ? compare_number : 10;
        return 1.0f * res / compare_number;
    }

}
