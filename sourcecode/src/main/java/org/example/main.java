package org.example;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.NullLiteralExpr;

import javax.security.auth.Destroyable;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class main
{
    public static int sum_clone_pairs = 0;
    public static int thread_number = 1;
    public static List<String> file_list = new ArrayList<>();
    public static List<Integer> order_list = new ArrayList<>();
    public static List<Func> func_list = new ArrayList<>();

    public static int partition_number = 1;
    public static float filter_score = 0.15f;
    public static float final_verify_score = 0.65f;
    public static String filedir = "/Users/haoranyan/git_rep/tamer/data/input";
    public static HashMap<String, Integer> string2char = new HashMap<>();
    public static HashMap<String, Integer> name_list = new HashMap<>();
    public static int work_type = 0;
    public static boolean readfile(String filepath) throws FileNotFoundException, IOException {
        try {
            File file = new File(filepath);
            if (!file.isDirectory()) {
                file_list.add(file.getAbsolutePath());}

            else if (file.isDirectory()) {
                String[] filelist = file.list();

                for (int i = 0; i < filelist.length; i++) {
                    File readfile = new File(filepath + "/" + filelist[i]);
                    if (!readfile.isDirectory()) {
                        file_list.add(readfile.getAbsolutePath());}
                    else if (readfile.isDirectory()) {
                        readfile(filepath + "/" + filelist[i]);
                    }
                }

            }

        } catch (FileNotFoundException e) {
            System.out.println("readfile()   Exception:" + e.getMessage());
        }
        return true;
    }
    public static synchronized void updateInvertedIndex(Func func, HashMap<Integer, HashSet<Integer>> invertedBox)
    {
        var ngramHash = func.ngramHash;
        for (var hash : ngramHash) {
            if (invertedBox.containsKey(hash)) {
                invertedBox.get(hash).add(func.funcorder);
            }
            else {
                HashSet<Integer> hs = new HashSet<>();
                hs.add(func.funcorder);
                invertedBox.put(hash, hs);
            }
        }
    }

    private static void copyFileUsingFileStreams(File source, File dest)
            throws IOException {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
        } finally {
            input.close();
            output.close();
        }
    }

    private static String readToString(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        InputStream list_input = new FileInputStream("/Users/haoranyan/git_rep/tamer/tmp/type");
        Scanner input_command = new Scanner(list_input);
        work_type = input_command.nextInt();
        File file_2 = new File("/Users/haoranyan/git_rep/tamer/result/output2");
        FileWriter g_fw = new FileWriter(file_2, true);
        BufferedWriter g_out = new BufferedWriter(g_fw);

        try {
            readfile(filedir);
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        }

        name_list.put("WhileStmt", 0);
        name_list.put("IfStmt", 1);
        name_list.put("SwitchStmt", 2);
        name_list.put("TryStmt", 3);
        name_list.put("ForEachStmt", 4);
        name_list.put("ThrowStmt", 5);
        name_list.put("ForStmt", 6);
        name_list.put("DoStmt", 7);
        name_list.put("SynchronizedStmt", 8);
        string2char.put("UnknownType", 0);
        string2char.put("CompilationUnit", 1);
        string2char.put("ClassOrInterfaceDeclaration", 2);
        string2char.put("MethodDeclaration", 3);
        string2char.put("ConstructorDeclaration", 4);
        string2char.put("Parameter", 5);
        string2char.put("ClassOrInterfaceType", 6);
        string2char.put("BlockStmt", 7);
        string2char.put("ExpressionStmt", 8);
        string2char.put("IfStmt", 9);
        string2char.put("ReturnStmt", 10);
        string2char.put("VariableDeclarationExpr", 11);
        string2char.put("UnaryExpr", 12);
        string2char.put("BinaryExpr", 13);
        string2char.put("NameExpr", 14);
        string2char.put("VariableDeclarator", 15);
        string2char.put("InstanceOfExpr", 16);
        string2char.put("SwitchStmt", 17);
        string2char.put("AssignExpr", 18);
        string2char.put("MethodCallExpr", 19);
        string2char.put("FieldAccessExpr", 20);
        string2char.put("SwitchEntry", 21);
        string2char.put("EnclosedExpr", 22);
        string2char.put("CastExpr", 23);
        string2char.put("ObjectCreationExpr", 24);
        string2char.put("TypeParameter", 25);
        string2char.put("ArrayType", 26);
        string2char.put("TryStmt", 27);
        string2char.put("ForEachStmt", 28);
        string2char.put("CatchClause", 29);
        string2char.put("WildcardType", 30);
        string2char.put("MarkerAnnotationExpr", 31);
        string2char.put("ArrayCreationExpr", 32);
        string2char.put("WhileStmt", 33);
        string2char.put("ThrowStmt", 34);
        string2char.put("ArrayCreationLevel", 35);
        string2char.put("ForStmt", 36);
        string2char.put("ArrayAccessExpr", 37);
        string2char.put("ClassExpr", 38);
        string2char.put("DoStmt", 39);
        string2char.put("ArrayInitializerExpr", 40);
        string2char.put("ExplicitConstructorInvocationStmt", 41);
        string2char.put("ConditionalExpr", 42);
        string2char.put("SingleMemberAnnotationExpr", 43);
        string2char.put("FieldDeclaration", 44);
        string2char.put("SynchronizedStmt", 45);
        string2char.put("AssertStmt", 46);
        string2char.put("InitializerDeclaration", 47);
        string2char.put("ThisExpr", 48);
        string2char.put("LabeledStmt", 49);
        string2char.put("BreakStmt", 50);
        string2char.put("ContinueStmt", 51);
        string2char.put("LocalClassDeclarationStmt", 52);
        string2char.put("SuperExpr", 53);
        string2char.put("NormalAnnotationExpr", 54);
        string2char.put("MemberValuePair", 55);
        string2char.put("Name", 56);
        string2char.put("LambdaExpr", 57);
        string2char.put("SimpleName", 58);
        string2char.put("Modifier", 59);
        string2char.put("PrimitiveType", 60);
        string2char.put("IntegerLiteralExpr", 61);
        string2char.put("NullLiteralExpr", 62);
        string2char.put("VoidType", 63);
        string2char.put("StringLiteralExpr", 64);
        string2char.put("BooleanLiteralExpr", 65);
        string2char.put("DoubleLiteralExpr", 66);
        string2char.put("CharLiteralExpr", 67);
        string2char.put("LongLiteralExpr", 68);
        string2char.put("EmptyStmt", 69);

        for (int kk = 1; kk <= 1; kk++) {
            TaskList<Func> funcTaskList = new TaskList<>();
            HashMap<Integer, HashSet<Integer>> invertedBox = new HashMap<>();
            for (int nnn = 15; nnn >= 15; nnn -= 1) {
                int parseWorkload = file_list.size() / thread_number + (file_list.size() % thread_number != 0 ? 1 : 0);
                AtomicInteger countnumber = new AtomicInteger();
                int totalsize = file_list.size();
                TaskList<String> parseTaskList = new TaskList<>(file_list);
                ArrayList<Thread> parseThreadList = new ArrayList<>();
                for (int i = 0; i < thread_number; i++) {
                    var finalStartIndex = i * parseWorkload;
                    var finalStopIndex = Math.min((i + 1) * parseWorkload, file_list.size());
                    int finalNnn = nnn;
                    var thread = new Thread(() -> {
                        CompilationUnit cu1;
                        String fn = parseTaskList.getTask();
                        while (fn != null) {
                            try {
                                String code = readToString(fn);
                                int flag = 0;
                                if (flag == 0) {
                                    if (code.indexOf(" class ") != -1)
                                        code = code.substring(code.indexOf(" class ") + 1);
                                }
                                else
                                {code = "class _a {" + code + "}";}
                                cu1 = StaticJavaParser.parse(code);

                                Func newfun = new Func(fn, finalNnn, string2char, name_list, cu1);
                                cu1 = null;
                                newfun.funcorder = funcTaskList.size();
                                funcTaskList.addItem(newfun);
                                func_list.add(newfun);
                                order_list.add(newfun.funcId);
                                updateInvertedIndex(newfun, invertedBox);
                                fn = parseTaskList.getTask();

                            } catch (Exception e) {
                                fn = parseTaskList.getTask();
                            }

                }

            });
            thread.start();
            parseThreadList.add(thread);
        }
        for (var thread : parseThreadList) {                  try {
                        thread.join();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                System.out.println(funcTaskList.size());
                Map<Integer, HashSet<Integer>> clonePairs = new HashMap<>();
                AtomicLong totalClonePairsNum = new AtomicLong();
                var partitionSize = funcTaskList.size() / partition_number + (funcTaskList.size() % partition_number != 0 ? 1 : 0);
                for (int i = 0; i < partition_number; i++) {
                    var startIndex = i * partitionSize;
                    var stopIndex = Math.min((i + 1) * partitionSize, funcTaskList.size());
                    var totalWorkload = stopIndex - startIndex;

                    ArrayList<Thread> detectThreadList = new ArrayList<>();
                    for (int j = 0; j < thread_number; j++) {
                        var thread = new Thread(() -> {
                            var funcC = funcTaskList.getTask();
                            while (funcC != null) {
                                HashSet<Integer> cloneCandidate = new HashSet<>();
                                HashSet<Integer> res = new HashSet<>();
                                int order = funcC.funcorder;
                                int i1 = funcTaskList.size();
                                int functimeslist[] = new int[i1 + 1];
                                for (var onegramHash : funcC.ngramHash) {
                                    if (invertedBox.containsKey(onegramHash)) {
                                        for (var everyone : invertedBox.get(onegramHash))
                                        {
                                            functimeslist[everyone]++;

                                        }
                                    }
                                }
                                for (int newconut = 0; newconut < i1; newconut++) {
                                    if ((work_type == 1 || work_type == 3) || (functimeslist[newconut] >= 1 && newconut > order)) {
                                        try {
                                            var funcB = funcTaskList.getItem(newconut);
                                            if (funcB.funcId == funcC.funcId)
                                                continue;
                                           // if (work_type == 3 && funcC.funcId != 1)
                                             //   continue;
                                            var nGramVerifyScore = Func.nLineVerify(funcC, funcB, invertedBox, functimeslist);
                                            // work type 1: 1 cmp 1
                                            // work type 3: cwe check
                                            if (work_type == 1 || work_type == 3) {
                                                funcC.Caculate_similarity_of_Func(funcB, work_type);
                                            }
                                            // work_type 2: n cmp n in one dataset
                                            // work_type 4: n cmp n but in two dataset
                                            else if (work_type == 2) {
                                                if (nGramVerifyScore >= 0.5) {
                                                    res.add(funcB.funcId);
                                                    sum_clone_pairs += 1;
                                                    funcC.Caculate_similarity_of_Func(funcB, work_type);
                                                    int score = (int)((nGramVerifyScore + 0.0005) * 1000);
                                                    if (score >= 1000) {
                                                        Random r = new Random();
                                                        score = 1000 - r.nextInt(100);
                                                    }
                                                    g_out.write(Integer.toString(score) + " ");
                                                    g_out.flush();
                                                } else if (nGramVerifyScore >= filter_score) {
                                                    var finalscore = funcC.Caculate_similarity_of_Func(funcB, work_type);
                                                    if (finalscore >= final_verify_score) {
                                                        res.add(funcB.funcId);
                                                        sum_clone_pairs += 1;
                                                        int score = (int)((finalscore + 0.0005) * 1000);
                                                        if (score >= 1000) {
                                                            Random r = new Random();
                                                            score = 1000 - r.nextInt(100);
                                                        }
                                                        g_out.write(Integer.toString(score) + " ");
                                                        g_out.flush();
                                                    }
                                                }
                                                //else {
                                                //    g_out.write(Integer.toString((int)((nGramVerifyScore + 0.0005) * 1000)) + " ");
                                                //    g_out.flush();
                                                //}
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            return;
                                        }
                                    }
                                }
                                if (!res.isEmpty()) {
                                    clonePairs.put(funcC.funcId, res);
                                }
                                funcC = funcTaskList.getTask();
                            }
                        });
                        thread.start();
                        detectThreadList.add(thread);
                    }
                    for (var thread : detectThreadList) {
                        try {
                            thread.join();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    String res_file = "/Users/haoranyan/git_rep/tamer/tmp_data.py";
                    File file_py = new File(res_file);
                    if (!file_py.exists())
                        file_py.createNewFile();
                    FileWriter fw = new FileWriter(file_py, false);
                    PrintWriter pw = new PrintWriter(fw);
                    //System.out.println(clonePairs);
                    //System.out.println(sum_clone_pairs);
                    pw.print("clone_pairs = [");
                    pw.flush();
                    //pw.print(clonePairs);
                    for (Integer key : clonePairs.keySet()) {
                        pw.print(key);
                        pw.flush();
                        pw.print(", ");
                        pw.flush();
                        pw.print(clonePairs.get(key));
                        pw.flush();
                        pw.print(", ");
                        pw.flush();
                    }
                    pw.print("]");
                    pw.flush();
                    pw.println();
                    pw.flush();
                    pw.print("sum_clone_pairs = ");
                    pw.flush();
                    pw.println(sum_clone_pairs);
                    pw.flush();
                    pw.close();
                    fw.close();
                }
            }
        }
        if (work_type == 2) {
            g_fw.close();
        }
    }



}
