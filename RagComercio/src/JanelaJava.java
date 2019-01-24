import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JOptionPane;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class JanelaJava extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private JButton btnMonitorar;
	private boolean monitorar = false;
	private JTextField textField_Preco;
	private JTextPane textPane;
	private Item itemMonitorado;
	private TrayIcon trayIcon;
	private static JanelaJava frame;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame = new JanelaJava();
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
	public JanelaJava() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 449, 328);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblDigiteOItem = new JLabel("Digite o item a ser monitorado:");
		lblDigiteOItem.setBounds(10, 11, 170, 14);
		contentPane.add(lblDigiteOItem);
		
		textField = new JTextField();
		textField.setBounds(198, 8, 206, 20);
		contentPane.add(textField);
		textField.setColumns(10);
		
		btnMonitorar = new JButton("Monitorar");
		btnMonitorar.setBounds(234, 65, 89, 23);
		contentPane.add(btnMonitorar);
		
		textField_Preco = new JTextField();
		textField_Preco.setText("0");
		textField_Preco.setBounds(198, 34, 206, 20);
		contentPane.add(textField_Preco);
		textField_Preco.setColumns(10);
		
		textPane = new JTextPane();
		textPane.setBounds(31, 99, 373, 131);
		contentPane.add(textPane);
		
		JButton btnEsconder = new JButton("Esconder");
		btnEsconder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
			}
		});
		btnEsconder.setBounds(315, 255, 89, 23);
		contentPane.add(btnEsconder);
		
		btnMonitorar.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				if(monitorar == false)
				{
					itemMonitorado = criaItem();
					if(itemMonitorado == null) {
						JOptionPane.showMessageDialog(null, "Insira um valor");
						return;
					}
					Thread custListLoadThread = new Thread(new webCrawlerTask());
					custListLoadThread.start();
					btnMonitorar.setText("Parar");
					monitorar = true;
				}
				else
				{
					monitorar = false;
					btnMonitorar.setText("Monitorar");
				}
			}
		});
		
		createSystemTray();
	}
	
	private Item criaItem()
	{
		Item item = new Item();
		item.setNome(textField.getText());
		String preco = textField_Preco.getText();
		if(preco.equals(""))
			return null;
		item.setPrecoMenor(Integer.parseInt(preco));
		return item;
	}
	
	private String createUrl()
	{
		String url= "https://www.ragcomercio.com/search/2/";
		String nomeItem = textField.getText().replaceAll("\\ ", "+").toLowerCase();
		nomeItem = unAccent(nomeItem);
		url = url + nomeItem;
		return url;
	}
	
	//Trecho de Codigo que nao e meu em partes 
	//-----------------------------------
	
	public static String unAccent(String s) {
	    //
	    // JDK1.5
	    //   use sun.text.Normalizer.normalize(s, Normalizer.DECOMP, 0);
	    //
	    String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
	    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	    return pattern.matcher(temp).replaceAll("");
	  }
	
	private void createSystemTray()
	{

		 if (!SystemTray.isSupported()) {
	         System.out.println("SystemTray is not supported");
	         return;
	     }
	     PopupMenu popup = new PopupMenu();
	     //Image image = Toolkit.getDefaultToolkit().getImage("icon.png");
	     Image image = new ImageIcon(this.getClass().getResource("icon.png")).getImage();
	     trayIcon = new TrayIcon(image, "Monitor de Itens");
	     SystemTray tray = SystemTray.getSystemTray();
	    
	     // Create a pop-up menu components
	     MenuItem aboutItem = new MenuItem("Abrir");
	     MenuItem exitItem = new MenuItem("Sair");
	    
	     aboutItem.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				frame.setVisible(true);
			}
		 });
	     exitItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				 System.exit(0);
			}
		 });
	     //Add components to pop-up menu
	     popup.add(aboutItem);
	     popup.addSeparator();
	     popup.add(exitItem);
	    
	     trayIcon.setPopupMenu(popup);
	    
	     try {
	         tray.add(trayIcon);
	     } catch (AWTException e) {
	         System.out.println("TrayIcon could not be added.");
	     }
	}
	
	private void displayTray() {
        //Obtain only one instance of the SystemTray object
        //SystemTray tray = SystemTray.getSystemTray();

        //If the icon is a file
        //Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
        //Alternative (if the icon is on the classpath):
        //Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("icon.png"));

        //TrayIcon trayIcon = new TrayIcon(image, "Item Encontrado");
        //Let the system resize the image if needed
        //trayIcon.setImageAutoSize(true);
        //Set tooltip text for the tray icon
        //trayIcon.setToolTip("Item");
        //tray.add(trayIcon);

        trayIcon.displayMessage("Um item foi encontrado", "Acesse o o site RagComercio", MessageType.INFO);
    }
	//-------------------------------------
	//Fim
	
	private class webCrawlerTask implements Runnable
	{
		public void run() {
			while(monitorar == true)
			{
				String url = createUrl();
				try {
					textPane.setText("Buscando...");
					Document document =  Jsoup.connect(url).get(); //conecta na url
					Elements elements = document.select("div#results"); //acha a divisao de resultados
					Element element = elements.select("table").first(); //acha a primeira tabela
					Elements items = element.select("tbody").select("tr"); //seleciona os elementos
					List<Item> itemsObj = new ArrayList<Item>();
					for(int i=0;i<items.size();i++)
					{
						String nome = items.select("a.itemdropdown").get(i).text(); //acha o nome dos elemetos
						int precoMenor = Integer.parseInt(items.select("span.label.label-success").get(i).text().replaceAll("\\.", "")); //acha o preco menor sem os pontos
						Item novoItem = new Item();
						novoItem.setNome(nome);
						novoItem.setPrecoMenor(precoMenor);
						itemsObj.add(novoItem);
						if(itemMonitorado.getNome().equals(novoItem.getNome()))
						{
							if(itemMonitorado.getPrecoMenor() >= novoItem.getPrecoMenor())
							{
								displayTray();
							}
						}
					}
					String texto = "";
					for(int i=0; i<itemsObj.size();i++)
					{
						texto = texto +"Nome: " + itemsObj.get(i).getNome() + " | Preço: " + itemsObj.get(i).getPrecoMenor() + "\n";
					}
					textPane.setText(texto);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				try {
					Thread.sleep(300000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					monitorar = false;
				}
			}
		}
	}
}
