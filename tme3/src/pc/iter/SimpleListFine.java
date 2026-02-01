package pc.iter;
import pc.IList;

public class SimpleListFine<T> implements IList<T> {
	private Chainon<T> head;
	
	private static class Chainon<T> {
		T data; // Donnée du chaînon
		Chainon<T> next; // Référence au chaînon suivant

		public Chainon(T data) {
			this.data = data;
			// NB : next est null par défaut
		}
		
		public boolean contains(T element) { 
			synchronized (this) { 
				if (data.equals(element)) { 
					return true; 
				} 
				else if (next == null) { 
					return false; 
				} 
			} 
			return next.contains(element); 
		}

	}
	
	public SimpleListFine() {
		head = null;
	}
	
	public boolean contains(T element) { 
		Chainon<T> cur; 
		synchronized (this) { 
			if (head == null) { 
				return false; 
			}
			else { 
				cur = head; 
			} 
		} 
		return cur.contains(element); 
	} 
	

	@Override
	public int size() {
		// TODO Auto-generated method stub
		synchronized (this){ 
			int size=0;
			Chainon<T> cur = head;	
			while (cur != null) {   
				size++; 
				cur = cur.next; 
			}
		return size;
		}
			
	}

	@Override
	public void add(T element) {
		
		synchronized (this) { 
			if(head==null) {
				head=new Chainon<>(element);
				return;
			}
		
			Chainon<T> cur=head;
	
			while (cur.next != null) { 
					cur = cur.next; 
			} 
			cur.next=new Chainon<>(element);
		}
		
	}


	@Override
	public void clear() {
		// TODO Auto-generated method stub
		synchronized(this) {
			head=null;
		}
		
	}
	
	public String toString() {
	    StringBuilder s = new StringBuilder("[");
	    
	    for (Chainon<T> cur = head; cur != null; cur = cur.next) {
	        synchronized (cur) {  // Synchronisation sur le chaînon courant
	            s.append(cur.data);  // Ajoute l'élément actuel
	        }

	        // Vérifie si cur.next n'est pas null seulement après avoir mis à jour cur
	        if (cur.next != null) {  
	            s.append(", ");
	        }
	    }
	    
	    s.append("]");  // Ferme la liste
	    return s.toString();
	}
}
