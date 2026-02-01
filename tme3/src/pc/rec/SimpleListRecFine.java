package pc.rec;
import pc.IList;

public class SimpleListRecFine<T> implements IList<T> {
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


		public void add(T element) {
			// TODO Auto-generated method stub
			synchronized(this) {
				if(next==null) {
					next=new Chainon<>(element);
				}
				else {
					next.add(element);					
				}
			}
		}

		public int size() {
			// TODO Auto-generated method stub
			synchronized (this) { 
				if(next==null) {
					return 1;
				}
				else {
					return 1+next.size();
				}
			}
		}
		
		public void toString(StringBuilder s) {
			synchronized(this) {
				if(next!=null) {
					s.append(data).append(",");
					next.toString(s);
				}
				else {
					s.append(data);
				}
			}
		}
	}
	
		@Override
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
		synchronized (this) {
			if(head==null) {
				return 0;
			}
			else {
				return head.size();				
			}
		}
	}

	@Override
	public void add(T element) {
		// TODO Auto-generated method stub
		synchronized(this) {
			if(head==null) {
				head=new Chainon<>(element);
			}
			else {
				head.add(element);
			}
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
		synchronized(this){
			StringBuilder s = new StringBuilder("[");
			if(head!=null) {
				head.toString(s);
			}
		return s.append("]").toString();
		}
	}

}
