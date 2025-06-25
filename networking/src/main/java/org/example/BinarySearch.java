package org.example;

public class BinarySearch {
    public boolean search(int n, int[] nums){
        int start = 0;
        int end = nums.length -1;
        while(start <= end){
            int mid = start + (end - start)/2;
            if(n == nums[mid]) return true;
            else if(n > nums[mid]){
                start = mid + 1;
            }
            else{
                end = mid - 1;
            }
        }
        return false;
    }

    public static void main(String[] args){
        BinarySearch bs = new BinarySearch();
        int[] nums = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22};
        System.out.println(bs.search(140,nums));
    }
}
