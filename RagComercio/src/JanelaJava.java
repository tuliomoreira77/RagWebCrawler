import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import javax.swing.JButton;

public class JanelaJava extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private JButton btnMonitorar;
	private boolean monitorar = false;
	private JTextField textField_Preco;
	private JTextPane textPane;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JanelaJava frame = new JanelaJava();
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
		setBounds(100, 100, 450, 300);
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
		btnMonitorar.setBounds(248, 76, 89, 23);
		contentPane.add(btnMonitorar);
		
		textField_Preco = new JTextField();
		textField_Preco.setBounds(222, 39, 150, 20);
		contentPane.add(textField_Preco);
		textField_Preco.setColumns(10);
		
		textPane = new JTextPane();
		textPane.setBounds(21, 119, 383, 131);
		contentPane.add(textPane);
		
		btnMonitorar.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				if(monitorar == false)
				{
					monitorar = true;
					new webCrawlerTask().run();
				}
				else
				{
					monitorar = false;
				}
			}
		});
	}
	
	private class webCrawlerTask implements Runnable
	{
		public void run() {
			//while(monitorar == true)
			{
				String url = "https://www.ragcomercio.com/search/2/manual+de+combate";
				try {
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
					}

					String texto = "";
					for(int i=0;i<itemsObj.size();i++)
					{
						texto = texto +"Nome: " + itemsObj.get(i).getNome() + " | Preço: " + itemsObj.get(i).getPrecoMenor() + "\n";
					}
					
					textPane.setText(texto);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
