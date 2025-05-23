package GUI;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GUI {

	public static void main(String[] args) {
		
		JFrame 프레임 = new JFrame();
		프레임.setTitle("J-UNITED CHAT");
		프레임.setSize(500,800);
		프레임.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//-------------------기본-----------
		
		프레임.setLocationRelativeTo(null);
		JPanel 패널1  = new JPanel();
		JPanel 패널2  = new JPanel();
	    JButton 버튼1 = new JButton("색다른버튼");
	    JButton 버튼2 = new JButton("큰버튼");
	    JButton 버튼3 = new JButton("버튼3");
	    JButton 버튼4 = new JButton("버튼4");
	    JButton 버튼5 = new JButton("버튼5");
	    JButton 버튼6 = new JButton("버튼6");
	    JButton 버튼7 = new JButton("버튼7");
	    JButton 버튼8 = new JButton("버튼8");
	      
	    패널1.add(버튼1);
	    패널1.add(버튼2);
	    패널1.add(버튼3);
	    패널1.add(버튼4);
	    패널2.add(Box.createVerticalStrut(10)); //세로간격10
	    패널2.add(버튼5);
	    패널2.add(Box.createVerticalStrut(10));
	    패널2.add(버튼6);
	    패널2.add(Box.createVerticalStrut(10));
	    패널2.add(버튼7);
	    패널2.add(Box.createVerticalStrut(10));
	    패널2.add(버튼8);
	      
	    프레임.add(패널1);
	    프레임.add(패널2);
		
	    패널2.setLayout(new BoxLayout(패널2, BoxLayout.Y_AXIS)); // 세로정렬
		버튼1.setBackground(Color.pink);
		패널1.setBackground(Color.blue);
		패널2.setBackground(Color.yellow); //색깔변경
		버튼2.setPreferredSize(new Dimension(75, 40));
		프레임.add(패널1, "North");  //북쪽으로 패널 정렬, 버튼에 크기맞춤
		프레임.add(패널2, "West");   // 서쪽
		
		프레임.setVisible(true); //시각화
		
		
	}

}
