/*
 * Used to keep track of how threads were scheduled to be able to reconstruct.
 */
public class history_node {

	public double		time;
	public int			mem_domain_id;
	public int			core_id;
	public history_node next; //pointer to next node in list.
	
	/*
	 * Only used by the bootstrap module where degradations are known on
	 * a per tick basis.
	 */
	public double	perf_degrad;
 
}
