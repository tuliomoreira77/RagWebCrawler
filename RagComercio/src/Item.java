
public class Item {
	
	private String nome;
	private int precoMenor;
	
	public Item()
	{
		
	}
	
	public Item(String nome, int precoMenor)
	{
		this.nome = nome;
		this.precoMenor = precoMenor;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public int getPrecoMenor() {
		return precoMenor;
	}
	public void setPrecoMenor(int precoMenor) {
		this.precoMenor = precoMenor;
	}
}
