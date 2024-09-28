package pc.rec;

import pc.IList;

public class SimpleListRecSync<T> implements IList<T> {
	private Chainon<T> head; // Premier élément de la liste

	private static class Chainon<T> {
		T data; // Donnée du chaînon
		Chainon<T> next; // Référence au chaînon suivant

		public Chainon(T data) {
			this.data = data;
			// NB : next est null par défaut
		}

		public synchronized int size() {
			if (next == null) {
				return 1;
			} else {
				return 1 + next.size();
			}
		}

		public synchronized boolean contains(T element) {
			if (data.equals(element)) {
				return true;
			} else if (next == null) {
				return false;
			} else {
				return next.contains(element);
			}
		}

		public synchronized void add(T element) {
			if (next == null) {
				next = new Chainon<>(element);
			} else {
				next.add(element);
			}
		}
		
		public synchronized void toString(StringBuilder s) {
			if(next!=null) {
				s.append(data).append(",");
				next.toString(s);
			}
			else {
				s.append(data);
			}
		}

	}

	public synchronized void clear() {
        head = null;
    }

	public synchronized int size() {
		if (head == null) {
			return 0;
		} else {
			return head.size();
		}
	}

	public synchronized boolean contains(T element) {
		if (head == null) {
			return false;
		} else {
			return head.contains(element);
		}
	}

	public synchronized void add(T element) {
		if (head == null) {
			head = new Chainon<>(element);
		} else {
			head.add(element);
		}
	}
	
	public synchronized String toString() {
		StringBuilder s = new StringBuilder("[");
		if(head!=null) {
			head.toString(s);
		}
		return s.append("]").toString();
	}

}
