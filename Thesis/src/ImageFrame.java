import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.JLayeredPane;
import javax.swing.BoxLayout;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Component;


public class ImageFrame extends JFrame {

	private JPanel contentPane;
	
	private final JFileChooser chooser;
	private BufferedImage image = null;
	private QuadTreePartition partition = null;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		//OpenCV starter stuff
		configureOpenCV();
		
		
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ImageFrame frame = new ImageFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ImageFrame() {
		
		
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 500, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
		
		this.setTitle("Exterkamp Thesis");	
		//this.setSize(width,height);
		addMenu();		
		chooser = new JFileChooser();		
		chooser.setCurrentDirectory(new File("."));	
		
	}
	
	private void addMenu(){
		
		JMenu fileMenu = new JMenu("File");		
		JMenuItem openItem = new JMenuItem("Open");	
		
		openItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				open();			
			}		
		});
		
		fileMenu.add(openItem);
		
		JMenuItem exitItem = new JMenuItem("Exit");
		
		exitItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){			
				System.exit(0);			
			}		
		});
		
		JMenu imageMenu = new JMenu("Image");	
		JMenuItem qItem = new JMenuItem("QuadTree");
		
		qItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if (image != null){
					partition = new QuadTreePartition(image);
					partition.partition();
					displayBufferedImage(QuadTreePartition.getImage());
					
					
				}
				
				
				
			}		
		});
		
		imageMenu.add(qItem);
		
		JMenuItem biItem = new JMenuItem("Bilinear Blur 75%");
		
		biItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if (image != null){
					image = scaleDown50(image);
					displayBufferedImage(image);
					
				}
				
				
				
			}		
		});
		
		imageMenu.add(biItem);
		
		JMenuItem keyItem = new JMenuItem("Keypoint Looker");
		
		keyItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if (image != null){
					image = keypointFinder(image);
					displayBufferedImage(image);
				}
				
				
				
			}		
		});
		
		imageMenu.add(keyItem);
		
		JMenuItem MSEItem = new JMenuItem("MSE looker");
		
		MSEItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if (image != null){
					image = mseMatch(image);
					displayBufferedImage(image);
				}
				//System.out.println("click");
				
				
			}		
		});
		
		imageMenu.add(MSEItem);
		
		JMenuItem Item90 = new JMenuItem("90 rotate");
		
		Item90.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if (image != null){
					AffineTransform tx = new AffineTransform();
					tx.translate(image.getHeight() * 0.5,image.getWidth() * 0.5);
					tx.rotate((Math.PI * 1.0) / 2.0);
					// first - center image at the origin so rotate works OK
					tx.translate(-image.getWidth() * 0.5,-image.getHeight() * 0.5);
					AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
					
					BufferedImage newImage = new BufferedImage(image.getHeight(), image.getWidth(), image.getType());
					newImage = op.filter(image, newImage);
					image = newImage;
					displayBufferedImage(newImage);
				}
				//System.out.println("click");
				
				
			}		
		});
		
		imageMenu.add(Item90);
		
		fileMenu.add(exitItem);		
		JMenuBar menuBar = new JMenuBar();		
		menuBar.add(fileMenu);	
		menuBar.add(imageMenu);
		this.setJMenuBar(menuBar);		
	}
	
	private void open(){
	
		File file = getFile();
		if (file != null){		
			displayFile( file);		
		}	
	}
	
	private File getFile(){
	
		File file = null;		
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
			file = chooser.getSelectedFile();
		}
		return file;	
	}
	
	private void displayFile(File file){
	
		try{
			displayBufferedImage(ImageIO.read(file));		
		}
		
		catch (IOException exception){		
			JOptionPane.showMessageDialog(this, exception);		
		}	
	}
	
	public void displayBufferedImage(BufferedImage image){
		this.image = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		this.image.getGraphics().drawImage(image,0,0,null);
		this.setContentPane(new JScrollPane (new JLabel(new ImageIcon(image))));	
		this.validate();	
	}
	
	private static void configureOpenCV(){
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
	    //Mat mat = Mat.eye( 3, 3, CvType.CV_8UC1 );
	    //System.out.println( "mat = " + mat.dump() );
	}
	
	private BufferedImage scaleDown50(BufferedImage input){
		int width = input.getWidth()/2;
		int height =  input.getHeight()/2;
		BufferedImage newImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
	    Graphics2D g = newImage.createGraphics();
	    try {
	        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
	                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	        g.setBackground(Color.WHITE);
	        g.clearRect(0, 0, width, height);
	        g.drawImage(input, 0, 0, width, height, null);
	    } finally {
	        g.dispose();
	    }
		
		
		return scaleUp50(newImage);
	}
	
	private BufferedImage scaleUp50(BufferedImage input){
		int width = input.getWidth()*2;
		int height =  input.getHeight()*2;
		BufferedImage newImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
	    Graphics2D g = newImage.createGraphics();
	    try {
	        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
	                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	        g.setBackground(Color.WHITE);
	        g.clearRect(0, 0, width, height);
	        g.drawImage(input, 0, 0, width, height, null);
	    } finally {
	        g.dispose();
	    }
	    return newImage;
	}
	
	private BufferedImage keypointFinder(BufferedImage input){
		
		int minHessian = 400;
		FeatureDetector dect = FeatureDetector.create(FeatureDetector.SURF);
		byte[] px = ((DataBufferByte) input.getRaster().getDataBuffer()).getData();
		Mat mat = new Mat(input.getHeight(),input.getWidth(),CvType.CV_8UC3);
		mat.put(0, 0, px);
		
		Imgproc.cvtColor(mat,mat,Imgproc.COLOR_RGB2GRAY);
		
		MatOfKeyPoint keypoints_1 = new MatOfKeyPoint();
		
		dect.detect(mat, keypoints_1);
		
		Features2d.drawKeypoints(mat, keypoints_1, mat);
		
		DescriptorExtractor desc =DescriptorExtractor.create(DescriptorExtractor.SURF);//SURF = 2
		
		Mat descriptors = new Mat();
		
		desc.compute(mat,keypoints_1, descriptors);
		
		int type;
		
		if(mat.channels() == 1)
            type = BufferedImage.TYPE_BYTE_GRAY;
        else
            type = BufferedImage.TYPE_3BYTE_BGR;
		
		byte[] data = new byte[input.getWidth() * input.getHeight() * (int)mat.elemSize()];
		mat.get(0, 0, data);
		
        BufferedImage newImg = new BufferedImage(input.getWidth(), input.getHeight(), type);

        newImg.getRaster().setDataElements(0, 0, input.getWidth(), input.getHeight(), data);
        
        return newImg;
		
	}

	private BufferedImage mseMatch(BufferedImage input){
		//System.out.println("mse matching");
		BufferedImage refImg = null;
		try {
		    refImg = ImageIO.read(new File("ref_8_by_8.jpg"));
		} catch (IOException e) {
		}
		//BufferedImage scaledImg = new BufferedImage(input.getWidth(),input.getHeight(), BufferedImage.TYPE_INT_ARGB);
		//Graphics2D g2d = refImg.createGraphics();
		//g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		//g2d.drawImage(refImg,0,0,input.getWidth(),input.getHeight(),null);
		//g2d.dispose();
		double LowestMSE = 1000000000;
		
		double sum_sq = 0;
		double mse;
		
		Graphics2D g2d = (Graphics2D) image.getGraphics();
		g2d.setColor(Color.RED);
		
		//System.out.print("scaled ref: " + scaledImg.getHeight() + " , " + scaledImg.getWidth());
		//System.out.println(" inputs: " + input.getHeight() + " , " + input.getWidth());
		
		int width = refImg.getWidth();
		int height = refImg.getHeight();
		
		ArrayList<AffineStorage> points = new ArrayList<AffineStorage>();
		
		for (int outerX = 0;outerX < (input.getWidth() - width);outerX += 2){//width){
			//System.out.print("outer x: " + outerX);
			for (int outerY = 0;outerY < (input.getHeight() - height);outerY += 2){//height){
				//System.out.print(" outer y: " + outerY);
				AffineTransform tx = new AffineTransform();
				for (int angle = 0; angle < 4; angle++){
					tx.translate(height * 0.5,width * 0.5);
					tx.rotate((Math.PI * angle) / 2);
					// first - center image at the origin so rotate works OK
					tx.translate(-width * 0.5,-height * 0.5);
					AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
					
					BufferedImage newImage = new BufferedImage(height, width, refImg.getType());
					op.filter(refImg, newImage);
					
					for (int i = 0; i < height && i + outerY < input.getHeight(); i++)
					{	
						//System.out.print(" inner y: " + i);
					    for (int j = 0; j < width && j + outerX < input.getWidth(); j++)
					    {
					    	
					    	//System.out.println(" inner x: " + j);
					        int p1 = newImage.getRGB(j, i);
					        
					        int p2 = input.getRGB(outerX + j, outerY + i);
					        
					        int r1 = (p1 >> 16) & 0xff;
					        int g1 = (p1 >> 8) & 0xff;
					        int b1 = (p1 >> 0) & 0xff;
					        int r2 = (p2 >> 16) & 0xff;
					        int g2 = (p2 >> 8) & 0xff;
					        int b2 = (p2 >> 0) & 0xff;
					        
					        
					        int err = p2 - p1;
					        int rerr = r2 - r1;
					        int gerr = g2 - g1;
					        int berr = b2 - b1;
					        if (rerr < 0){rerr *= -1;};
					        if (gerr < 0){gerr *= -1;};
					        if (berr < 0){berr *= -1;};
					        //sum_sq += (err * err);
					        
					        double sq = ((rerr + gerr + berr)/3.0);
					        
					        
					        
					        //System.out.println(sq);
					        sum_sq += (sq);
					    }
					}
					
					sum_sq /= (width * height);
					if (sum_sq < 15 && sum_sq > -15){
						//LowestMSE = mse;
						int imageUpperLeftY = outerY;//input.getHeight() - outerY;
						//g2d.drawRect(outerX, imageUpperLeftY, width, height);
						//g2d.drawImage(refImg, outerX, imageUpperLeftY,null);
						//g2d.drawImage(refImg, outerX, imageUpperLeftY,outerX + width, imageUpperLeftY + height,
								//0,0,width,height,null);
						System.out.println(outerX + " , " + outerY + "/" + imageUpperLeftY + " " + sum_sq);
						
						//points.add(new Point2D.Double(outerX,imageUpperLeftY));
						
						
						points.add(new AffineStorage(new Point2D.Double(outerX,imageUpperLeftY),new AffineTransform(tx),1));
						
						//return new boolBuff(false,scaledImg);
					}
					
					
					mse = (double)sum_sq / (width * height);
					sum_sq = 0;
					//if (mse < 0){
					//	mse *= -1;
					//}
					
					/*if (false){//mse < 0.01  && mse > -0.01 ){
						LowestMSE = mse;
						int imageUpperLeftY = input.getHeight() - outerY;
						g2d.drawRect(outerX, imageUpperLeftY, width, height);
						System.out.println(outerX + " , " + outerY + "/" + imageUpperLeftY + " " + mse);
						//return new boolBuff(false,scaledImg);
					}*/
				
				}
				
			}
		}
		
		for (AffineStorage point : points){
			BufferedImage newImage = new BufferedImage(height, width, refImg.getType());
			AffineTransformOp op = new AffineTransformOp(point.getTransform(), AffineTransformOp.TYPE_BILINEAR);
			op.filter(refImg, newImage);
			g2d.drawImage(newImage,(int)point.getPoint().getX(),(int)point.getPoint().getY(),null);
			//g2d.drawRect(outerX, imageUpperLeftY, width, height);
		}
		
		return image;
		//return new boolBuff(false);
	}
	
}
