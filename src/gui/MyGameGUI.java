package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JFileChooser;
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
import utils.Point3D;
import utils.StdDrawGame;

public class MyGameGUI  {
	public  graph graph;
	Hashtable<Point3D, Fruit> fruits;
	Hashtable<Integer, Robot> robots;
	private static final long serialVersionUID = 6128157318970002904L;
	LinkedList<Point3D> points = new LinkedList<Point3D>();
	double X_min = Integer.MAX_VALUE;
	double X_max = Integer.MIN_VALUE;
	double Y_min = Integer.MAX_VALUE;
	double Y_max = Integer.MIN_VALUE;

	public MyGameGUI(){
		this.graph =null;
		initGUI();

	}


	public MyGameGUI(graph g)
	{
		this.graph=g;
		initGUI();
	}

	public MyGameGUI(graph g , Fruit f , Robot r)
	{
		this.graph=g;
		initGUI();
	}



	public void Scenario() {
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
				game_service game = Game_Server.getServer(sen); // you have [0,23] games
				String g = game.getGraph();
				DGraph gg = new DGraph();
				gg.init(g);
				this.graph =gg;
				String info = game.toString();
				// fruit
				Fruit f = new Fruit();
				if (fruits == null ) {
					fruits= new Hashtable<Point3D, Fruit>();
				}
				for (String  fruit : game.getFruits()) {
					f.init(fruit);
					Point3D p_f	=f.getPoint3D();
					fruits.put(p_f, f);
				}
				//robot
				Robot r=new Robot();
				JSONObject obj = new JSONObject(info);
				JSONObject Robot =obj.getJSONObject("GameServer");
				int robot = Robot.getInt("robots");
				Collection<node_data> node = graph.getV();
				int size = node.size();
				int rnd = 0;
				if (robots == null ) {
					robots = new Hashtable<Integer, Robot>();
				}
				for ( int i =0 ; i < robot ; i++) {
					do {
						rnd = (int) (Math.random()*size);
						if (graph.getNode(rnd) != null) {
							Point3D po = graph.getNode(rnd).getLocation();
							game.addRobot(rnd);
						}
					} while (graph.getNode(rnd) == null);
				}
				for (String robo : game.getRobots()) {
					r.init(robo);
					int id = r.getID();
					robots.put(id, r);
				}
				initGUI();
				startGameNow(game, gg);
			}
			
		}
		catch (Exception e) {
			// TODO: handle exception
		}			
	}

	public void startGameNow(game_service game ,graph gg) {
		game.startGame();
		while(game.isRunning()) {
			moveRobots(game , gg);
		}
	}
	private void moveRobots(game_service game, graph gg) {
		List<String> log = game.move();
		if(log!=null) {
			long t = game.timeToEnd();
			for(int i=0;i<log.size();i++) {
				String robot_json = log.get(i);
				try {
					JSONObject line = new JSONObject(robot_json);
					JSONObject ttt = line.getJSONObject("Robot");
					int rid = ttt.getInt("id");
					Robot r =robots.get(rid);
					int src = ttt.getInt("src");
					r.setSrc(src);
//					r.setPoint3D(graph.getNode(src).getLocation());
					int dest = ttt.getInt("dest");
					r.setDest(dest);
					
					if(dest==-1) {	
						dest = nextNode(gg, src);
						game.chooseNextEdge(rid, dest);
						r.setPoint3D(graph.getNode(dest).getLocation());
						System.out.println("Turn to node: "+dest+"  time to end:"+(t/1000));
						System.out.println(ttt);
					}
					paint();
				} 
				catch (JSONException e) {e.printStackTrace();}
			}
		}
	}

	private static int nextNode(graph g, int src) {
		int ans = -1;
		Collection<edge_data> ee = g.getE(src);
		Iterator<edge_data> itr = ee.iterator();
		int s = ee.size();
		int r = (int)(Math.random()*s);
		int i=0;
		while(i<r) {itr.next();i++;}
		ans = itr.next().getDest();
		return ans;
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
			paintFruit();
			paintRobot();
			StdDrawGame.show();
		}
	}

	public void paintFruit() {
		if ( this.fruits != null ) {
			Set <Point3D> set = fruits.keySet();
			for (Point3D p3 : set) {
				Fruit fru = fruits.get(p3);
//				Point3D po = fru.getPoint3D();
				if (fru.getType() == -1) {
				StdDrawGame.setPenColor(Color.RED);
				StdDrawGame.filledCircle(p3.x(), p3.y(), 0.0001);
				}
				else {
					StdDrawGame.setPenColor(Color.CYAN);
					StdDrawGame.filledCircle(p3.x(), p3.y(), 0.0001);
				}
//				StdDrawGame.setPenColor(Color.BLACK);
//				StdDrawGame.text(p3.x(), p3.y()+(p3.y()*0.000004) , (Integer.toString(fru.getValue())));
			}
		}
	}



	public void moveRobot(game_service game ) {
		Thread t= new Thread(new Runnable() {

			@Override
			public void run() {
				while(game.isRunning()) {

				}
			}
		});

	}

	public void paintRobot() {
		if(this.robots!=null) {
			Set <Integer> set = robots.keySet();
			for (Integer robot : set) {
				Robot robo = robots.get(robot);
				Point3D p = robo.getPoint3D();
				StdDrawGame.setPenColor(Color.GREEN);
				StdDrawGame.filledCircle(p.x(), p.y(), 0.0001); //nodes in orange
				StdDrawGame.setPenColor(Color.BLACK);
				StdDrawGame.text(p.x(), p.y()+(p.y()*0.000004) , (Integer.toString(robo.getValue())));
			}

		}
	}

	public void initGUI() {
		StdDrawGame.setCanvasSize(800, 600);
		StdDrawGame.enableDoubleBuffering();
		double X_min = Integer.MAX_VALUE;
		double X_max = Integer.MIN_VALUE;
		double Y_min = Integer.MAX_VALUE;
		double Y_max = Integer.MIN_VALUE;

		// rescale the coordinate system
		if (graph != null) {
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
