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
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;

public class JanelaJava extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private JButton btnBuscar;
	private TrayIcon trayIcon;
	private static JanelaJava frame;
	private JList<String> list;
	JList<String> listBusca;
	private JButton btnRemover;
	private List<Item> listaMonitorados = new ArrayList<Item>();
	int lastSize = 0;

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
		setBounds(100, 100, 818, 328);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblDigiteOItem = new JLabel("Digite o item a ser monitorado:");
		lblDigiteOItem.setBounds(90, 11, 240, 14);
		contentPane.add(lblDigiteOItem);
		
		textField = new JTextField();
		textField.setBounds(90, 36, 206, 20);
		contentPane.add(textField);
		textField.setColumns(10);
		
		btnBuscar = new JButton("Buscar");
		btnBuscar.setBounds(144, 67, 89, 23);
		contentPane.add(btnBuscar);
		
		btnBuscar.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				Thread custListLoadThread = new Thread(new buscaTask());
				custListLoadThread.start();
			}
		});
		
		JButton btnEsconder = new JButton("Esconder");
		btnEsconder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
			}
		});
		btnEsconder.setBounds(10, 264, 89, 23);
		contentPane.add(btnEsconder);
		
		JLabel lblItemsMonitorados = new JLabel("Items Monitorados:");
		lblItemsMonitorados.setBounds(517, 39, 177, 14);
		contentPane.add(lblItemsMonitorados);
		
		DefaultListModel<String> model = new DefaultListModel<>();
		list = new JList<String>(model);
		list.setBounds(517, 70, 255, 171);
		contentPane.add(list);
		
		btnRemover = new JButton("Remover");
		btnRemover.setBounds(396, 151, 89, 23);
		
		btnRemover.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String itemRemover = list.getSelectedValue();
				((DefaultListModel<String>) list.getModel()).removeElement(itemRemover);
				int index = getListIndexByName(itemRemover);
				listaMonitorados.remove(index);
			}
		});
		contentPane.add(btnRemover);
		
		JButton btnMonitorar = new JButton("Monitorar");
		btnMonitorar.setBounds(396, 117, 89, 23);
		contentPane.add(btnMonitorar);
		btnMonitorar.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Item itemMonitorado = new Item();
				itemMonitorado.setNome(listBusca.getSelectedValue());
				String preco = JOptionPane.showInputDialog(null, "Insira o limite inferior:");
				itemMonitorado.setPrecoMenor(Integer.parseInt(preco));
				listaMonitorados.add(itemMonitorado);
				((DefaultListModel<String>) list.getModel()).addElement(itemMonitorado.getNome());
				if(listaMonitorados.size() == 1)
				{
					Thread monitoraThread = new Thread(new monitorTask());
					monitoraThread.start();
				}
			}
		});
		
		DefaultListModel<String> modelBusca = new DefaultListModel<>();
		listBusca = new JList<String>(modelBusca);
		listBusca.setBounds(37, 103, 332, 138);
		contentPane.add(listBusca);
		
		createSystemTray();
	}
	
	private int getListIndexByName(String nome)
	{
		for(int i=0; i < listaMonitorados.size(); i++)
		{
			if(listaMonitorados.get(i).getNome().equals(nome))
			{
				return i;
			}
		}
		
		return -1;
	}
	
	private Item criaItem()
	{
		Item item = new Item();
		item.setNome(textField.getText());
		return item;
	}
	
	private String createUrl(String nome)
	{
		String url= "https://www.ragcomercio.com/search/2/";
		String nomeItem = nome.replaceAll("\\ ", "+").toLowerCase();
		nomeItem = unAccent(nomeItem);
		url = url + nomeItem;
		return url;
	}
	
	private boolean hasNewItem()
	{
		if(lastSize < listaMonitorados.size())
		{
			lastSize = listaMonitorados.size();
			return true;	
		}
		else
		{
			lastSize = listaMonitorados.size();
			return false;
		}
	}
	
	private List<Item> accesUrl(Item itemAtual)
	{
		String url = createUrl(itemAtual.getNome());
		List<Item> itemsObj = new ArrayList<Item>();
		try {
			Document document =  Jsoup.connect(url).get(); //conecta na url
			Elements elements = document.select("div#results"); //acha a divisao de resultados
			Element element = elements.select("table").first(); //acha a primeira tabela
			Elements items = element.select("tbody").select("tr"); //seleciona os elementos
			for(int i=0;i<items.size();i++)
			{
				String nome = items.select("a.itemdropdown").get(i).text(); //acha o nome dos elemetos
				int precoMenor = Integer.parseInt(items.select("span.label.label-success").get(i).text().replaceAll("\\.", "")); //acha o preco menor sem os pontos
				Item novoItem = new Item();
				novoItem.setNome(nome);
				novoItem.setPrecoMenor(precoMenor);
				itemsObj.add(novoItem);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return itemsObj;
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
	
	private void displayNotification(Item item) {

        trayIcon.displayMessage("O item: "+ item.getNome() + " foi encontrado.", "Acesse o o site RagComercio", MessageType.INFO);
    }
	//-------------------------------------
	//Fim
	private class buscaTask implements Runnable
	{

		public void run() {
			
			Item item = criaItem();
			List<Item> items = accesUrl(item);
			if(!((DefaultListModel<String>)listBusca.getModel()).isEmpty())
				((DefaultListModel<String>)listBusca.getModel()).removeAllElements();
			
			for(int i=0; i<items.size();i++)
			{
				((DefaultListModel<String>) listBusca.getModel()).addElement(items.get(i).getNome());
			}
		}
	}
	
	private class monitorTask implements Runnable
	{
		public void run() {
			System.out.println("Iniciei Minhas Monitorações");
			while(listaMonitorados.size() > 0)
			{
				int count = 0;
				while(!hasNewItem())
				{
					if(count == 150 || listaMonitorados.size() == 0)
						break;
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
					e.printStackTrace();
					}
					count++;
				}
				System.out.println("Recomeçar!");
				for(int j=0; j<listaMonitorados.size();j++)
				{
					Item itemAtual = listaMonitorados.get(j);
					List<Item> items = accesUrl(itemAtual);
					
					for(int i=0;i<items.size();i++)
					{
						if(items.get(i).getPrecoMenor() < itemAtual.getPrecoMenor())
							displayNotification(itemAtual);
					}
					
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
					e.printStackTrace();
					}
				}
			}
			System.out.println("Finalizei Minhas Monitorações");
		}
	}
}
