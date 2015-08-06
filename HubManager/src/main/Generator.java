/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package word.grid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 *
 * @author safuser
 */
public class Generator {
    
    Random rand = new Random();
    final int[] VALUES = new int[]{8, 7, 1, 2, 1, 3, 2, 1, 6, 4, 1, 6, 4, 9, 3, 9, 8, 2, 4, 5, 2, 5, 2, 6, 9, 4, 2, 9, 
            4, 2, 2, 3, 1, 8, 1, 4, 3, 8, 1, 2,1, 5, 7, 7, 2, 7, 4, 4, 5, 6, 2, 9, 3, 4, 6, 5, 8, 7, 2, 9, 8, 4, 3, 3};
    final List<String> WORDLIST = Arrays.asList("AMAZON", "WEB", "SERVICES", "ELASTIC", "COMPUTE", "CLOUD", "VIRTUAL", "MACHINE", "IMAGE", "SECURITY", "NETWORK", "ACCOUNT", "PASSWORD", "USER", "CONSOLE", "XEN", "HYPERVISOR", "GUEST", "SOFTWARE", "MONITOR", "RDS");
        
    char[] highestBoard = new char[64];
    int highestBoardValue = 0; 
    // Note: A=65-64=1 B=66-64=2 etc...
        
    public void run(int loops, String output, int printFreq) {
        for (int x = 0; x < loops; x++) { // Number of attempts to make
            
            // Print number of boards processed if enabled
            if (output.equals("status") && x % printFreq == 0) {
                System.out.println("" + x + " boards processed");
            }
            
            // Copy WORDLIST for temporary use
            ArrayList<String> wordlist = new ArrayList<>();
            for (String s : this.WORDLIST) {
                wordlist.add(s);
            }
            
            char[] board = new char[64];
            int location = -1; // The last location a character was added to, starts at -1 bc the first char will be added to 0
            int score = 0;
            
            // Add words to the board
            for (int w = 0; 0 < this.WORDLIST.size(); w++) {
                if (wordlist.isEmpty()) {
                    break;
                }
                
                // Get the next word to use
                String word = wordlist.remove(this.rand.nextInt(wordlist.size()));
                if (location + word.length() > 63) {
                    continue;
                }
                
                // Add word to the board
                for (int i = 0; i < word.length(); i++) {
                    location++;
                    board[location] = word.charAt(i);
                }
                
                // Chance for a space
                if (this.rand.nextInt(2) == 1) {
                    location++;
                    if (location == 64) {
                        continue;
                    }
                    board[location] = ' ';
                }
            }
            
            // Calculate score
            for (int loc = 0; loc < this.VALUES.length; loc++) {
                if (board[loc] != ' ') {
                    int charVal = (int)board[loc] - 64;
                    score += this.VALUES[loc] * charVal;
                }
            }
            
            // Print each card if full logging mode is on
            if (output.equals("full")) {
                String printString = "";
                for (int i = 0; i < board.length; i++) {
                    if (i % 8 == 0) {
                        printString += "\n";
                    }
                    printString += board[i];
                }
               printString += "\n\nScore: " + score + "\n";

                System.out.print(printString);
            }
            
            // Check if it is the highest
            if (score > this.highestBoardValue) {
                for (int c = 0; c < board.length; c++) {
                    this.highestBoard[c] = board[c];
                }
                this.highestBoardValue = score;
                
                if (output.equals("highestvalue")) {
                    print();
                }
            }
        }
    }
    
    public void print() {
        String printString = "";
        for (int i = 0; i < this.highestBoard.length; i++) {
            if (i % 8 == 0) {
                printString += "\n";
            }
            printString += this.highestBoard[i];
        }
        printString += "\n\nScore: " + this.highestBoardValue + "\n";
        
        System.out.print(printString);
    }
}
