package ai;

import ai.Global;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import kalaha.*;

/**
 * This is the main class for your Kalaha AI bot. Currently
 * it only makes a random, valid move each turn.
 * 
 * @author Johan Hagelbäck
 */
public class AIClient implements Runnable
{
    private int player;
    private JTextArea text;
    
    private PrintWriter out;
    private BufferedReader in;
    private Thread thr;
    private Socket socket;
    private boolean running;
    private boolean connected;
    	
    /**
     * Creates a new client.
     */
    public AIClient()
    {
	player = -1;
        connected = false;
        
        //This is some necessary client stuff. You don't need
        //to change anything here.
        initGUI();
	
        try
        {
            addText("Connecting to localhost:" + KalahaMain.port);
            socket = new Socket("localhost", KalahaMain.port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            addText("Done");
            connected = true;
        }
        catch (Exception ex)
        {
            addText("Unable to connect to server");
            return;
        }
    }
    
    /**
     * Starts the client thread.
     */
    public void start()
    {
        //Don't change this
        if (connected)
        {
            thr = new Thread(this);
            thr.start();
        }
    }
    
    /**
     * Creates the GUI.
     */
    private void initGUI()
    {
        //Client GUI stuff. You don't need to change this.
        JFrame frame = new JFrame("My AI Client");
        frame.setLocation(Global.getClientXpos(), 445);
        frame.setSize(new Dimension(420,250));
        frame.getContentPane().setLayout(new FlowLayout());
        
        text = new JTextArea();
        JScrollPane pane = new JScrollPane(text);
        pane.setPreferredSize(new Dimension(400, 210));
        
        frame.getContentPane().add(pane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setVisible(true);
    }
    
    /**
     * Adds a text string to the GUI textarea.
     * 
     * @param txt The text to add
     */
    public void addText(String txt)
    {
        //Don't change this
        text.append(txt + "\n");
        text.setCaretPosition(text.getDocument().getLength());
    }
    
    /**
     * Thread for server communication. Checks when it is this
     * client's turn to make a move.
     */
    public void run()
    {
        String reply;
        running = true;
        
        try
        {
            while (running)
            {
                //Checks which player you are. No need to change this.
                if (player == -1)
                {
                    out.println(Commands.HELLO);
                    reply = in.readLine();

                    String tokens[] = reply.split(" ");
                    player = Integer.parseInt(tokens[1]);
                    
                    addText("I am player " + player);
                }
                
                //Check if game has ended. No need to change this.
                out.println(Commands.WINNER);
                reply = in.readLine();
                if(reply.equals("1") || reply.equals("2") )
                {
                    int w = Integer.parseInt(reply);
                    if (w == player)
                    {
                        addText("I won!");
                    }
                    else
                    {
                        addText("I lost...");
                    }
                    running = false;
                }
                if(reply.equals("0"))
                {
                    addText("Even game!");
                    running = false;
                }

                //Check if it is my turn. If so, do a move
                out.println(Commands.NEXT_PLAYER);
                reply = in.readLine();
                if (!reply.equals(Errors.GAME_NOT_FULL) && running)
                {
                    int nextPlayer = Integer.parseInt(reply);

                    if(nextPlayer == player)
                    {
                        out.println(Commands.BOARD);
                        String currentBoardStr = in.readLine();
                        boolean validMove = false;
                        while (!validMove)
                        {
                            long startT = System.currentTimeMillis();
                            //This is the call to the function for making a move.
                            //You only need to change the contents in the getMove()
                            //function.
                            GameState currentBoard = new GameState(currentBoardStr);
                            int cMove = getMove(currentBoard);
                            
                            //Timer stuff
                            long tot = System.currentTimeMillis() - startT;
                            double e = (double)tot / (double)1000;
                            
                            out.println(Commands.MOVE + " " + cMove + " " + player);
                            reply = in.readLine();
                            if (!reply.startsWith("ERROR"))
                            {
                                validMove = true;
                                addText("Made move " + cMove + " in " + e + " secs");
                            }
                        }
                    }
                }
                
                //Wait
                Thread.sleep(100);
            }
	}
        catch (Exception ex)
        {
            running = false;
        }
        
        try
        {
            socket.close();
            addText("Disconnected from server");
        }
        catch (Exception ex)
        {
            addText("Error closing connection: " + ex.getMessage());
        }
    }
    
    /**
     * This is the method that makes a move each time it is your turn.
     * Here you need to change the call to the random method to your
     * Minimax search.
     * 
     * @param currentBoard The current board state
     * @return Move to make (1-6)
     */
    public int getMove(GameState currentBoard)
    {
    	
    	// GETTING MOVE BY ITERATIVE DEEPENING SEARCH// AIMING FOR B GRADE
    	return this.iterativeDeepeningSearch(currentBoard);
    	
    	//GETTING MOVE BY DEPTH FIRST SEARCH// AIMING FOR C GRADE
    	
    	/*int bestScore=Integer.MIN_VALUE;
    	int alpha=bestScore;
    	int beta=Integer.MAX_VALUE;
    	int move=1;
    	
    	 for(int i=1;i<7;i++){
    	 		if(currentBoard.moveIsPossible(i)){
    	  			GameState cloneboard = currentBoard.clone();
    	  			cloneboard.makeMove(i);
    	  			int score=Math.max(bestScore,depthFirstMinimax(cloneboard,10,true,alpha,beta));
    	  			if(score>bestScore){
    	  				bestScore=score;
    	  				move=i;
    	  			}
    	  		}
    	  }
    	  return move;
    	  */
    	 
    	 
        
    }
    
    /**
     * Returns a random ambo number (1-6) used when making
     * a random move.
     * 
     * @return Random ambo number
     */
    public int getRandom()
    {
        return 1 + (int)(Math.random() * 6);
    }
    
    
     /** 
     * minimax with depth first search, no time-limitation
     * @param currentBoard, the actual game state
     * @param profondeur(depth) of the search
     * @param isMaximizingPlayer, looks if this is the ai's turn or the opponent's turn
     * @param alpha for alpha-beta pruning
     * @param beta for alpha-beta pruning
     * 
     * @return bestScore, the best state possible given by the utility function
     */
    public int depthFirstMinimax(GameState currentBoard,int profondeur,boolean isMaximizingPlayer,int alpha,int beta) {
    	int bestScore;
    	int opponent=(this.player==1) ? 2 : 1;
    	
    	if(this.isSearchOver(currentBoard, profondeur)) { //if we are at a final node or if the game ended then returns the value of the node
    		return this.utilityFunction(currentBoard, player, opponent);
    	}
    	
    	if(isMaximizingPlayer) { 	// MAX player,  trying to maximize the score
    		bestScore=Integer.MIN_VALUE;
    		for(int i=1;i<=6;i++) {
    			if(currentBoard.moveIsPossible(i)) {
    				GameState cloneBoard = currentBoard.clone();// clone the game board to makes the moves
    				cloneBoard.makeMove(i); // make all the possible moves, that will create the game tree
    				bestScore=Math.max(bestScore,depthFirstMinimax(cloneBoard,profondeur-1,false,alpha,beta)); //simulate the opponent turn
    			}
    			alpha=Math.max(bestScore, alpha);
    			if(alpha>beta) { // alpha beta pruning
    				break;
    			}
    		}
    	}
    	else {			// MIN player, trying to minimize the score
    			bestScore=Integer.MAX_VALUE;
    			for(int i=1;i<=6;i++) {
    				if(currentBoard.moveIsPossible(i)) {
    					GameState cloneBoard = currentBoard.clone();
    					cloneBoard.makeMove(i);
    					bestScore=Math.min(bestScore,depthFirstMinimax(cloneBoard,profondeur-1,true,alpha,beta)); // same as above
    				}
    				beta=Math.min(bestScore,beta);
    				if(alpha>beta) { // alpha beta pruning
    					break;
    				}
    			
    			}
    		}
    	
    	return bestScore;
    	
    	}
 
    /**
     * iterative deepening search, perform a depth first search at level 1 and repeat it with incrementing the level by 1
     * each time the algorithm executes, with a limit duration of 5 seconds
     * @param currentBoard the current game state
     * @return move, the best move 
     */
    public int iterativeDeepeningSearch(GameState currentBoard) {

    	int bestScore=Integer.MIN_VALUE;
    	int alpha=Integer.MIN_VALUE;
    	int beta=Integer.MAX_VALUE;
    	int move=0;
    	long timeRemaining=5000;// the maximum amount of time in milliseconds that the iterative search cannot exceed
    	int level=1;
    	while(timeRemaining>100) {
    		if(level>100) {	//limit the depth 
    			break;
    		}
    		for(int i=1;i<7;i++) {
    			long start = System.currentTimeMillis(); // start of the current iteration
        		if(currentBoard.moveIsPossible(i)) {
        			if(currentBoard.getNoValidMoves(player)==1) {	// if there is only 1 possible move then do it
        				return i;
        			}
    	    			GameState cloneBoard = currentBoard.clone();
    	    			cloneBoard.makeMove(i); 
    	    			//for each move do a depth-first search with the depth specified
    	    			int score=Math.max(bestScore,depthFirstMinimax(cloneBoard,level,true,alpha,beta,timeRemaining)); 
    	    			if(score>bestScore) {	// if the move is better than the last, update the new bestScore and best move
    	    				bestScore=score;
    	    				move=i;
    				}
    	    			long checkTime = System.currentTimeMillis();	//check the time ellapsed
    	    			timeRemaining-=(checkTime-start);				// and update the remaining time
    			}
        		
    		}
    		level++;//increase the depth of the next depth-first search
    	}
    	return move;
    }
    /**
     * looks if the search is over or not
     * @param board the current game state
     * @param depth 
     * @return boolean
     */
    public boolean isSearchOver(GameState board, int depth) {
    	if(board.gameEnded() || depth==0) {
    		return true;
    	}
    	return false;
    }
    
    /**
     * evaluate how good is a state(node)
     * @param board the current game state
     * @param player the AI (max player)
     * @param opponent(min player)
     * @return the evaluation of a leaf node
     */
    public int utilityFunction(GameState board,int player,int opponent) {
    	return board.getScore(player)-board.getScore(opponent);
    }
    	
    /**
     * this is the depth first search version used by iterative deepening, with a time limitation parameter
     * @param currentBoard
     * @param profondeur
     * @param isMaximizingPlayer
     * @param alpha
     * @param beta
     * @param time
     * @return
     */
   
    public int depthFirstMinimax(GameState currentBoard,int profondeur,boolean isMaximizingPlayer,int alpha,int beta,long time) {
    	int bestScore;
    	int opponent=(this.player==1) ? 2 : 1;
    	long timeRemaining=time;
    	
    	long start=System.currentTimeMillis();	// start of the search
    	
    	if(this.isSearchOver(currentBoard, profondeur)) { //if we are at a final node or if the game ended then returns the value of the node
    		return this.utilityFunction(currentBoard, player, opponent);
    	}
    	
    	if(isMaximizingPlayer) {	// MAX player,  trying to maximize the score
    							
    		bestScore=Integer.MIN_VALUE;
    		for(int i=1;i<=6;i++) {
    			if(timeRemaining<0) {
    				break;
    			}
    			if(currentBoard.moveIsPossible(i)) {
    				GameState cloneBoard = currentBoard.clone();// clone the game board to makes the moves
    				cloneBoard.makeMove(i); // make all the possible moves, that will create the game tree
    				bestScore=Math.max(bestScore,depthFirstMinimax(cloneBoard,profondeur-1,false,alpha,beta,timeRemaining)); //simulate the opponent turn
    			}
    			alpha=Math.max(bestScore, alpha);
    			if(alpha>beta) { // alpha beta pruning
    				break;
    			}
    			long checkTime=System.currentTimeMillis();	//check the time ellapsed
    			timeRemaining-=(checkTime-start);			// and update remaining time
    		}
    	}
    	else {			// MIN player, trying to minimize the score
    			bestScore=Integer.MAX_VALUE;
    			for(int i=1;i<=6;i++) {
    				if(timeRemaining<0) {
        				break;
        			}
    				
    				if(currentBoard.moveIsPossible(i)) {
    					GameState cloneBoard = currentBoard.clone();
    					cloneBoard.makeMove(i);
    					bestScore=Math.min(bestScore,depthFirstMinimax(cloneBoard,profondeur-1,true,alpha,beta,timeRemaining)); // same as above
    				}
    				beta=Math.min(bestScore,beta);
    				if(alpha>beta) { // alpha beta pruning
    					break;
    				}
    				long checkTime=System.currentTimeMillis();
        			timeRemaining-=(checkTime-start);
    			}
    		}
    	
    	return bestScore;
    	
    	}
 
    
}