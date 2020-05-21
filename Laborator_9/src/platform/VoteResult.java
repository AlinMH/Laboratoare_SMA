package platform;

import java.io.Serializable;
import java.util.List;

public class VoteResult implements Serializable {
	
    private static final long serialVersionUID = 1L;
    
    List<Ballot> ballots;
    
	public VoteResult() {
		
	}

	/**
	 * @return the ballots
	 */
	public List<Ballot> getBallots() {
		return ballots;
	}

	/**
	 * @param ballots the ballots to set
	 */
	public void setBallots(List<Ballot> ballots) {
		this.ballots = ballots;
	}
	
}
