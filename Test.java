package HackAssembler;
import java.io.*;
import java.util.*;

public class Test {
         private final SymTranslate symbols;
         private int currLine;
         private Load loader;

         public Test() {
                  symbols = new SymTranslate();
                  currLine = 0;
         }

         private void loadFile(final String filename) {
                  try {
                           final BufferedReader input = new BufferedReader(new FileReader(filename));
                           boolean parseSuccess;
                           String line;

                           while ((line = input.readLine()) != null) {
                                    loader = new Load();
                                    parseSuccess = loader.readInput(line);

                                    if (parseSuccess) {
                                             if (line.trim().charAt(0) == '(') {
                                                      final String symbol = line.trim().substring(line.indexOf("(") + 1,
                                                                        line.lastIndexOf(")"));
                                                      if (!symbols.contains(symbol))
                                                               symbols.put(symbol, currLine);
                                                      currLine--;
                                             }
                                             currLine++;
                                    }
                           }
                           input.close();
                  } catch (final IOException ioe) {
                           System.out.println(ioe);
                           return;
                  }
         }

         private void storeHack(final String filePath) {
                  try {
                           int lastSeparator = filePath.lastIndexOf(File.separator);
                           String fileName = (lastSeparator != -1) ? filePath.substring(lastSeparator + 1) : filePath;

                           String outputFilename = fileName.substring(0, fileName.lastIndexOf(".")) + ".hack";

                           BufferedReader input = new BufferedReader(new FileReader(filePath));
                           PrintWriter output = new PrintWriter(outputFilename);

                           currLine = 0;
                           boolean parseSuccess;
                           String line;

                           while ((line = input.readLine()) != null) {
                                    loader = new Load();
                                    parseSuccess = loader.readInput(line);

                                    if (parseSuccess && line.trim().charAt(0) != '(') {
                                             if (loader.addr() == null) {
                                                      final String comp = DJCTranslate.loadComp(loader.computation());
                                                      final String dest = DJCTranslate.loadDest(loader.destination());
                                                      final String jump = DJCTranslate.loadJump(loader.jump());
                                                      output.printf("111%s%s%s\n", comp, dest, jump);
                                             } else {
                                                      final String var = loader.addr();

                                                      final Scanner sc = new Scanner(var);
                                                      if (sc.hasNextInt()) {
                                                               final String addrBinary = Integer
                                                                                 .toBinaryString(Integer.parseInt(var));
                                                               output.println(padB(addrBinary));
                                                      } else {
                                                               symbols.addVar(var);
                                                               final String addrBinary = Integer
                                                                                 .toBinaryString(symbols.get(var));
                                                               output.println(padB(addrBinary));
                                                      }
                                                      sc.close();
                                             }
                                             currLine++;
                                    }
                           }
                           input.close();
                           output.close();
                  } catch (final IOException ioe) {
                           System.out.println(ioe);
                           return;
                  }
         }

         private String padB(final String unpaddedBinary) {
                  String paddedBinary = "";
                  final int numZeros = 16 - unpaddedBinary.length();

                  for (int i = 0; i < numZeros; i++) {
                           paddedBinary += "0";
                  }

                  return paddedBinary + unpaddedBinary;
         }

         public static void main(final String[] args) {
                  final String filename = args[0];
                  final Test assembly = new Test();
                  assembly.loadFile(filename);
                  assembly.storeHack(filename);
         }
}

class Load {
         private String destination;
         private String computation;
         private String jump;
         private String addr;

         public Load() {
                  destination = "null";
                  jump = "null";
         }

         public boolean readInput(String line) {
                  line = line.trim();

                  if (!line.isEmpty()) {
                           if (line.charAt(0) != '/') {
                                    if (line.contains("@")) {
                                             addr = line.split("@")[1].trim();
                                    } else {
                                             if (line.contains("=")) {
                                                      final String[] fields = line.split("=");
                                                      destination = fields[0];
                                                      if (fields[1].contains(";")) {
                                                               sJump(fields[1]);
                                                      } else {
                                                               computation = fields[1].split("/")[0].trim();
                                                      }
                                             } else if (line.contains("+") || line.contains("-")) {
                                                      if (line.contains(";")) {
                                                               sJump(line);
                                                      } else {
                                                               computation = line.split("/")[0].trim();
                                                      }
                                             } else if (line.contains(";")) {
                                                      sJump(line);
                                             } else {
                                                      jump = line.split("/")[0].trim();
                                             }
                                    }
                                    return true;
                           }
                  }
                  return false;
         }

         private void sJump(final String str) {
                  final String[] parts = str.split(";");
                  computation = parts[0].trim();
                  jump = parts[1].split("/")[0].trim();
         }

         public String destination() {
                  return destination;
         }

         public String computation() {
                  return computation;
         }

         public String jump() {
                  return jump;
         }

         public String addr() {
                  return addr;
         }
}

class DJCTranslate {
         private static Hashtable<String, String> destTranslate = new Hashtable<String, String>(8);
         private static Hashtable<String, String> jumpTranslate = new Hashtable<String, String>(8);
         private static Hashtable<String, String> compTranslate = new Hashtable<String, String>(28);

         private static void initDestTranslate() {
                  destTranslate.put("null", "000");
                  destTranslate.put("M", "001");
                  destTranslate.put("D", "010");
                  destTranslate.put("MD", "011");
                  destTranslate.put("A", "100");
                  destTranslate.put("AM", "101");
                  destTranslate.put("AD", "110");
                  destTranslate.put("AMD", "111");
         }

         private static void initCompTranslate() {
                  compTranslate.put("0", "0101010");
                  compTranslate.put("1", "0111111");
                  compTranslate.put("-1", "0111010");
                  compTranslate.put("D", "0001100");
                  compTranslate.put("A", "0110000");
                  compTranslate.put("!D", "0001101");
                  compTranslate.put("!A", "0110001");
                  compTranslate.put("-D", "0001111");
                  compTranslate.put("-A", "0110011");
                  compTranslate.put("D+1", "0011111");
                  compTranslate.put("A+1", "0110111");
                  compTranslate.put("D-1", "0001110");
                  compTranslate.put("A-1", "0110010");
                  compTranslate.put("D+A", "0000010");
                  compTranslate.put("D-A", "0010011");
                  compTranslate.put("A-D", "0000111");
                  compTranslate.put("D&A", "0000000");
                  compTranslate.put("D|A", "0010101");
                  compTranslate.put("M", "1110000");
                  compTranslate.put("!M", "1110001");
                  compTranslate.put("-M", "1110011");
                  compTranslate.put("M+1", "1110111");
                  compTranslate.put("M-1", "1110010");
                  compTranslate.put("D+M", "1000010");
                  compTranslate.put("D-M", "1010011");
                  compTranslate.put("M-D", "1000111");
                  compTranslate.put("D&M", "1000000");
                  compTranslate.put("D|M", "1010101");
         }

         private static void initJumpTranslate() {
                  jumpTranslate.put("null", "000");
                  jumpTranslate.put("JGT", "001");
                  jumpTranslate.put("JEQ", "010");
                  jumpTranslate.put("JGE", "011");
                  jumpTranslate.put("JLT", "100");
                  jumpTranslate.put("JNE", "101");
                  jumpTranslate.put("JLE", "110");
                  jumpTranslate.put("JMP", "111");
         }

         public static String loadComp(final String key) {
                  initCompTranslate();
                  return compTranslate.get(key);
         }

         public static String loadDest(final String key) {
                  initDestTranslate();
                  return destTranslate.get(key);
         }

         public static String loadJump(final String key) {
                  initJumpTranslate();
                  return jumpTranslate.get(key);
         }
}

class SymTranslate {
         private int curr;
         private final Hashtable<String, Integer> table;

         public SymTranslate() {
                  curr = 16;
                  table = new Hashtable<String, Integer>(25);

                  for (int i = 0; i <= 15; i++) {
                           final String key = "R" + i;
                           table.put(key, i);
                  }

                  table.put("SP", 0);
                  table.put("LCL", 1);
                  table.put("ARG", 2);
                  table.put("THIS", 3);
                  table.put("THAT", 4);
                  table.put("SCREEN", 16384);
                  table.put("KBD", 24576);
         }

         public void put(final String symbol, final int value) {
                  table.put(symbol, value);
         }

         public boolean contains(final String symbol) {
                  return table.containsKey(symbol);
         }

         public int get(final String symbol) {
                  return table.get(symbol);
         }

         public boolean addVar(final String symbol) {
                  if (!table.containsKey(symbol)) {
                           table.put(symbol, curr);
                           curr++;
                           return true;
                  }

                  return false;
         }
}