package com.drin.java.clustering;

import java.util.Map;

import java.util.LinkedList;
import java.util.HashMap;

public class CandidateQueue {
   private LinkedList<CandidatePair> mQueue;
   private Map<String, Boolean> mRemovedCandidates;

   public CandidateQueue() {
      mQueue = new LinkedList<CandidatePair>();
      mRemovedCandidates = new HashMap<String, Boolean>();
   }

   public void addCandidate(CandidatePair newCandidate) {
      int ndx = 0;
      for (ndx = 0; ndx < mQueue.size() && mQueue.get(ndx).compareTo(newCandidate) > 0; ndx++) {
         ;
      }

      mQueue.add(ndx, newCandidate);
   }

   public void addAllCandidates(CandidateQueue otherQueue) {
      for (CandidatePair otherPair : otherQueue.mQueue) {
         addCandidate(otherPair);
      }
   }

   public CandidatePair dequeue() {
      CandidatePair bestCandidate = null;

      while (!mQueue.isEmpty()) {
         bestCandidate = mQueue.removeFirst();

         String leftCluster = bestCandidate.getLeftClusterName();
         String rightCluster = bestCandidate.getRightClusterName();

         //This is to be sure that we aren't returning stale/already combined
         //clusters
         if (!mRemovedCandidates.containsKey(leftCluster) &&
             !mRemovedCandidates.containsKey(rightCluster)) {
            mRemovedCandidates.put(leftCluster, new Boolean(true));
            mRemovedCandidates.put(rightCluster, new Boolean(true));
            break;
         }
         else { bestCandidate = null; }
      }

      return bestCandidate;
   }

   public CandidatePair peek() {
      CandidatePair bestCandidate = dequeue();

      if (bestCandidate != null) { addCandidate(bestCandidate); }
      return bestCandidate;
   }
}
