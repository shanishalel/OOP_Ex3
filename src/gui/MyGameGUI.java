package gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.json.JSONException;
import org.json.JSONObject;

import MYdataStructure.DGraph;
import MYdataStructure.edge_data;
import MYdataStructure.graph;
import MYdataStructure.node_data;
import Server.Game_Server;
import Server.game_service;
import algorithms.Graph_Algo;
import gameClient.Fruit;
import gameClient.Robot;
import gameClient.*;
import utils.Point3D;
import utils.StdDrawGame;

/**
 * This class represents the gui graph that we initalize , every MyGameGui have :
    public  graph graph; - graph object
	Hashtable<Point3D, Fruit> fruits; - hashtable for the fruits
	Hashtable<Integer, Robot> robots; - hashtable for the robots
	LinkedList<Point3D> points = new LinkedList<Point3D>(); - linked list of points
	double X_min = Integer.MAX_VALUE; - the x min value in the frame
	double X_max = Integer.MIN_VALUE; - the x max value in the frame
	double Y_min = Integer.MAX_VALUE; - the y min value in the frame
	double Y_max = Integer.MIN_VALUE; - the y max value in the frame
 * 
 * @author USER
 *
 */
public class MyGameGUI  {

	game_service game;
	public  graph graph;
	public Hashtable<Point3D, Fruit> fruits;
	public Hashtable<Integer, Robot> robots;
	private static final long serialVersionUID = 6128157318970002904L;
	public LinkedList<Point3D> points = new LinkedList<Point3D>();
	public double X_min = Integer.MAX_VALUE;
	public double X_max = Integer.MIN_VALUE;
	public double Y_min = Integer.MAX_VALUE;
	public double Y_max = Integer.MIN_VALUE;
	public double x;
	public double y;
	public boolean isRobot = false;
	public KML_Logger KML;

	/**
	 * This function is a default constructor
	 */
	public MyGameGUI(){
		this.graph =null;
		initGUI();

	}

	/**
	 * This function is constructor that get graph g and init int it to gui
	 * @param g
	 */
	public MyGameGUI(graph g)
	{
		this.graph=g;
		initGUI();
	}

	/**
	 * This function is constructor that gets graph g, fruit f, roobot r 
	 * @param g
	 * @param f
	 * @param r
	 */
	public MyGameGUI(graph g , Fruit f , Robot r)
	{
		this.graph=g;
		initGUI();
	}

	/**
	 * This function is playing the automatic choice, and sent it to moveautomatic
	 */
	public void Playautomatic() {

		JFrame input = new JFrame();
		String s ="";
		s = JOptionPane.showInputDialog(
				null, "Please enter a Scenario number between 0-23");

		int sen=Integer.parseInt(s);
		if(sen<0 || sen>23) {
			JOptionPane.showMessageDialog(input, "The number that you entered isn't a Scenario number " );
		}
		else {
			this.game = Game_Server.getServer(sen); // you have [0,23] games
			Automatic Auto= new Automatic (this);//call for autogame for this graph and string 
			Auto.Playautomatic(game, sen);
		}
		
	}

	/**
	 * This function gets the point that the user click on her in the screen
	 * @param x
	 * @param y
	 */
	public void setXY(double x , double y) {
		this.x=x;
		this.y=y;
	}

	/**
	 * This function moving the robots manualy by the user choice 
	 * while the game is running she will change the robots ocation by the location the user entered 
	 * and se will print the changes on the screen
	 * @param game
	 */
	
	public void PlayManual() {
		try {
			JFrame input = new JFrame();
			String s ="";
			s = JOptionPane.showInputDialog(
					null, "Please enter a Scenario number between 0-23");
			int sen=Integer.parseInt(s);
			if(sen<0 || sen>23) {
				JOptionPane.showMessageDialog(input, "The number that you entered isn't a Scenario number " );
			}
			else {
				this.game = Game_Server.getServer(sen); // you have [0,23] games
				String g = game.getGraph();
				DGraph gg = new DGraph();
				gg.init(g);
				graph =gg;
				String info = game.toString();
				
				Manual playMan=new Manual(this);
				 playMan.PlayManual(game, sen);
			}
		}
			catch(Exception e) {
				e.printStackTrace();
			}
	}

	/**
	 * this function will paint the graph 
	 */
	public void paint() {
		StdDrawGame.clear();
		if(this.graph !=null) {
			Collection <node_data> Nodes = this.graph.getV();
			for (node_data node_data : Nodes) {
				Point3D p = node_data.getLocation();
				StdDrawGame.setPenColor(Color.ORANGE);
				StdDrawGame.filledCircle(p.x(), p.y(), 0.0001); //nodes in orange
				StdDrawGame.setPenColor(Color.BLACK);

				StdDrawGame.text(p.x(), p.y()+(p.y()*0.000004) , (Integer.toString(node_data.getKey())));
				Collection<edge_data> Edge = this.graph.getE(node_data.getKey());
				for (edge_data edge_data : Edge) {
					if (edge_data.getTag() ==100) {
						edge_data.setTag(0);
						StdDrawGame.setPenColor(Color.RED); 
					}
					else {
						StdDrawGame.setPenColor(Color.BLUE);
					}
					node_data dest = graph.getNode(edge_data.getDest());
					Point3D p2 = dest.getLocation();
					if (p2 != null) {
						StdDrawGame.line(p.x(), p.y(), p2.x(), p2.y());
						StdDrawGame.setPenColor(Color.MAGENTA);
						double x_place =((((((p.x()+p2.x())/2)+p2.x())/2)+p2.x())/2);
						double y_place = ((((((p.y()+p2.y())/2)+p2.y())/2)+p2.y())/2);
						StdDrawGame.filledCircle(x_place, y_place, 0.0001);
						StdDrawGame.setPenColor(Color.BLUE);
						//cut the number to only 1 digit after the point
						String toShort=Double.toString(edge_data.getWeight());
						int i=0;
						while(i<toShort.length()) {
							if(toShort.charAt(i)=='.') {
								toShort=toShort.substring(0, i+2);
							}
							i++;
						}
						StdDrawGame.text(x_place, y_place-(y_place*0.000004),toShort );
					}	
				}
			}
			painStatus();
			paintFruit();
			paintRobot();
			StdDrawGame.show();
		}
	}

	/**
	 * This function will paint the fruit on the screen by pass over the 
	 * fruit location .
	 */
	public void paintFruit() {
		if ( this.fruits != null ) {
			Set <Point3D> set = fruits.keySet();
			for (Point3D p3 : set) {
				Fruit fru = fruits.get(p3);
				if (fru.getType() == -1) {
					StdDrawGame.setPenColor(Color.RED);
					StdDrawGame.picture(p3.x(), p3.y(), "data/boy.jpg", 0.0006, 0.0004);
					//					StdDrawGame.filledCircle(p3.x(), p3.y(), 0.00015);
				}
				else {
					StdDrawGame.setPenColor(Color.CYAN);
					StdDrawGame.picture(p3.x(), p3.y(), "data/girl.jpg", 0.0006, 0.0004);
					//					StdDrawGame.filledCircle(p3.x(), p3.y(), 0.00015);
				}
			}
		}
	}

	/**
	 * This function paint the robots by stdDraw
	 */
	public void paintRobot() {
		if(this.robots!=null) {
			Set <Integer> set = robots.keySet();
			for (Integer robot : set) {
				Robot robo = robots.get(robot);
				Point3D p = robo.getPoint3D();
				StdDrawGame.setPenColor(Color.GREEN);
				StdDrawGame.picture(p.x(), p.y(), "data/car2.jpg", 0.0008, 0.0004);
				//				StdDrawGame.picture(x, Y_max, filename, scaledWidth, scaledHeight);
				//				StdDrawGame.filledCircle(p.x(), p.y(), 0.00025);
			}
		}
	}
	
	public void painStatus() {
		try {
		StdDrawGame.setPenColor(Color.BLACK);
		String info = game.toString();
		JSONObject obj = new JSONObject(info);
		JSONObject GAME =obj.getJSONObject("GameServer");
		int grade = GAME.getInt("grade");
		StdDrawGame.text(X_max, Y_max,"grade: " +""+grade);
		StdDrawGame.text(X_max, Y_max-(Y_max*0.000009),"time: " +""+game.timeToEnd()/1000);
		StdDrawGame.text(X_max, Y_max-(Y_max*0.000018),"move: " +""+GAME.getInt("moves"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This function is init the gui 
	 */
	public void initGUI() {
		StdDrawGame.setCanvasSize(800, 600);
		StdDrawGame.enableDoubleBuffering();
		 X_min = Integer.MAX_VALUE;
		 X_max = Integer.MIN_VALUE;
		 Y_min = Integer.MAX_VALUE;
		 Y_max = Integer.MIN_VALUE;

		// rescale the coordinate system
		if (graph != null) {
			KML = new KML_Logger(graph);
			KML.kml_Graph();
			Collection<node_data> nodes=graph.getV();   
			for(node_data node:nodes) {
				if(node.getLocation().x()>X_max) {
					X_max=(node.getLocation().x());
				}
				if(node.getLocation().x()<X_min) {
					X_min=(node.getLocation().x());
				}
				if(node.getLocation().y()>Y_max) {
					Y_max=(node.getLocation().y());
				}
				if(node.getLocation().y()<Y_min) {
					Y_min=(node.getLocation().y());
				}

			}	
		}
		StdDrawGame.setXscale(X_min, X_max);
		StdDrawGame.setYscale(Y_min, Y_max);
		StdDrawGame.setGuiGraph(this);
		paint();
	}







}
