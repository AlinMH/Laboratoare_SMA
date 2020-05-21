package platform;

import java.io.Serializable;
import java.util.List;

public class Ballot implements Serializable {
	
    private static final long serialVersionUID = 1L;
	
    int count;
    List<String> candidates;
    
	public Ballot() {
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * @return the candidates
	 */
	public List<String> getCandidates() {
		return candidates;
	}

	/**
	 * @param candidates the candidates to set
	 */
	public void setCandidates(List<String> candidates) {
		this.candidates = candidates;
	}
	
	
}
