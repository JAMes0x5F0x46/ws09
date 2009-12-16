package at.tuwien.ifs.somtoolbox.util;

import java.util.Random;

/**
 *
 * @author neumayer
 * @date 17 Jan 2009
 * @deprecated substitute with commons-math.rand*
 **/

public class RandomTools {

	public static int[] permutation(Random random, int n){

		assert n > 0;
		//intitial element order is irrelevant so long as each int 1..n occurs exactly once
		//inorder initialization assures that is the case

		int[] sample = new int[n];
		for (int k = 0; k < sample.length; k++) {
			sample[k] = k; // + 1;
		}
		//loop invariant: the tail of the sample array is randomized.
		//Intitally the tail is empty; at each step move a random
		//element from front of array into the tail, then decrement boundary of tail
		int last = sample.length-1;   //last is maximal index of elements not in the tail

		while (last > 0){
			// Select random index in range 0..last, and swap its contents with those at last
			// The null swap is allowed; it should be possible that sample[k] does not change
			swap(random.nextInt(last+1), last, sample);
			last -= 1;
		}
		return sample;
	}
	
	private static void swap(int j, int k, int[] array){
		int temp = array[k];
		array[k] = array[j];
		array[j] = temp;
	}

}


