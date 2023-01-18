package avl;


/**
 * FibonacciHeap
 * <p>
 * An implementation of a Fibonacci Heap over integers.
 */
public class FibonacciHeap_v2 {

    public int size = 0;
    public HeapNode min;
    public HeapNode first;
    public HeapNode last;
    public static int numOfLinks = 0;

    private static int numOfCuts = 0;

    /**
     * public boolean isEmpty()
     * <p>
     * Returns true if and only if the heap is empty.
     */
    public boolean isEmpty() {   //O(1)
        return this.size == 0;
    }

    /**
     * public HeapNode insert(int key)
     * <p>
     * Creates a node (of type HeapNode) which contains the given key, and inserts it into the heap.
     * The added key is assumed not to already belong to the heap.
     * <p>
     * Returns the newly created node.
     */
    public HeapNode insert(int key) {  //O(1)
        HeapNode newNode = new HeapNode(key);
        this.insertNode(newNode);
        this.size += 1;
        return newNode;
    }

    /**
     * public void deleteMin()
     * <p>
     * Deletes the node containing the minimum key.
     */
    public void deleteMin() {   //WC O(n), Amortize O(logn)
        this.size -= 1;
        if (this.size == 0) {
            this.first = null;
            this.min = null;
            this.last = null;
            return;
        } else if (this.min.rank == 0) {
            deleteNoChild();
        } else if (this.min.rank > 0) {
            deleteWithChild();
        }
        this.consolidation();
    }

    /**
     * public HeapNode findMin()
     * <p>
     * Returns the node of the heap whose key is minimal, or null if the heap is empty.
     */
    public HeapNode findMin() {
        if (this.isEmpty()) {
            return null;
        }
        return this.min;
    }

    /**
     * public void meld (FibonacciHeap heap2)
     * <p>
     * Melds heap2 with the current heap.
     */
    public void meld(FibonacciHeap_v2 heap2) {  //O(1)
        if (this.isEmpty()){
            this.min=heap2.min; this.first=heap2.first; this.last=heap2.last; this.size=heap2.size; return;}
        if (heap2.isEmpty()){return;}
        this.last.next = heap2.first;
        heap2.first.prev = this.last;
        this.first.prev = heap2.last;
        heap2.last.next = this.first;
        this.last = heap2.last;
        this.size += heap2.size();
        if (heap2.min.key < this.min.key) {
            this.min = heap2.min;
        }}


    /**
     * public int size()
     * <p>
     * Returns the number of elements in the heap.
     */
    public int size() { //O(1)
        return this.size;
    }

    /**
     * public int[] countersRep()
     * <p>
     * Return an array of counters. The i-th entry contains the number of trees of order i in the heap.
     * (Note: The size of the array depends on the maximum order of a tree.)
     */
    public int[] countersRep() {
        int[] aidArray = new int[this.size];
        if (this.isEmpty()) {
            return aidArray;
        }
        aidArray[this.first.rank] += 1;
        HeapNode curNode = this.first.next;
        while (curNode.key != this.first.key) {
            aidArray[curNode.rank] += 1;
            curNode = curNode.next;
        }
        int index = 0;
        for (int i = aidArray.length - 1; i >= 0; i--) {
            if (aidArray[i] != 0) {
                index = i;
                break;
            }
        }
        int[] result = new int[index + 1];
        System.arraycopy(aidArray, 0, result, 0, result.length);
        return result;
    }

    /**
     * public void delete(HeapNode x)
     * <p>
     * Deletes the node x from the heap.
     * It is assumed that x indeed belongs to the heap.
     */
    public void delete(HeapNode x) {
        int delta = x.key-this.min.key+1;
        if (this.min.key==Integer.MIN_VALUE){this.decreaseKey(x, x.key-this.min.key);}
        else{this.decreaseKey(x, delta);}
        this.deleteMin();
    }

    /**
     * public void decreaseKey(HeapNode x, int delta)
     * <p>
     * Decreases the key of the node x by a non-negative value delta. The structure of the heap should be updated
     * to reflect this change (for example, the cascading cuts procedure should be applied if needed).
     */
    public void decreaseKey(HeapNode x, int delta) {
        x.key = x.key - delta;
        if (x.key == Integer.MIN_VALUE){this.min=x;}
        if (x.key<this.min.key){this.min=x;}
        if (x.parent != null) {
            if (x.key < x.parent.key) { //breaks the heap condition
                this.cascadingCut(x, x.parent);
            }
        }
    }

    /**
     * public int nonMarked()
     * <p>
     * This function returns the current number of non-marked items in the heap
     */
    public int nonMarked() {
        int result = 0;
        HeapNode curNode = this.first;
        boolean reachedLast = false;
        while (!reachedLast) {
            if (!curNode.mark) {
                result++;
            }
            if (curNode.rank > 0) {
                HeapNode childNode = curNode.child;
                for (int i = 0; i < curNode.rank; i++) {
                    result += markRec(childNode);
                    childNode = childNode.next;
                }
            }
            if (curNode.key == this.last.key) {
                reachedLast = true;
            }
            curNode = curNode.next;

        }
        return result;
    }

    /**
     * public int potential()
     * <p>
     * This function returns the current potential of the heap, which is:
     * Potential = #trees + 2*#marked
     * <p>
     * In words: The potential equals to the number of trees in the heap
     * plus twice the number of marked nodes in the heap.
     */
    public int potential() {
        if (this.isEmpty()) {
            return 0;
        }
        int marked = this.size - this.nonMarked();
        int numOfTrees = 1;
        HeapNode curNode = this.first.next;
        while (curNode.key != this.first.key) {
            numOfTrees++;
            curNode = curNode.next;
        }
        return numOfTrees + 2 * marked;
    }

    /**
     * public static int totalLinks()
     * <p>
     * This static function returns the total number of link operations made during the
     * run-time of the program. A link operation is the operation which gets as input two
     * trees of the same rank, and generates a tree of rank bigger by one, by hanging the
     * tree which has larger value in its root under the other tree.
     */
    public static int totalLinks() {
        return numOfLinks;
    }

    /**
     * public static int totalCuts()
     * <p>
     * This static function returns the total number of cut operations made during the
     * run-time of the program. A cut operation is the operation which disconnects a subtree
     * from its parent (during decreaseKey/delete methods).
     */
    public static int totalCuts() {
        return numOfCuts;
    }

    /**
     * public static int[] kMin(FibonacciHeap H, int k)
     * <p>
     * This static function returns the k smallest elements in a Fibonacci heap that contains a single tree.
     * The function should run in O(k*deg(H)). (deg(H) is the degree of the only tree in H.)
     * <p>
     * ###CRITICAL### : you are NOT allowed to change H.
     */
    public static int[] kMin(FibonacciHeap_v2 H, int k) {
        if (k==0){return new int[0];}
        int[] arr = new int[k];
        FibonacciHeap_v2 aidHeap = new FibonacciHeap_v2();
        aidHeap.insert(H.min.key);
        aidHeap.first.copy = H.min;
        for (int i = 0; i < k; i++) {
            arr[i] = aidHeap.min.key;
            if (aidHeap.min.copy.child != null){
                boolean last = false;
                HeapNode child = aidHeap.min.copy.child;
                while (!last){
                    aidHeap.insert(child.key);
                    aidHeap.first.copy = child;
                    child = child.next;
                    if ( child.key == aidHeap.min.copy.child.key) {last=true;}
                }
            }
            aidHeap.deleteMin();
        }
        return arr;
    }

    //Aid Functions

    public void deleteNoChild() {
        if (this.first.key == this.min.key) {
            this.first = this.min.next;
        }
        if (this.last.key == this.min.key) {
            this.last = this.min.prev;
        }
        this.min.prev.next = this.min.next;
        this.min.next.prev = this.min.prev;
    }

    public void deleteWithChild() {
        if (this.first.key == this.min.key) {
            this.first = this.min.child;
        }
        if (this.last.key == this.min.key) {
            this.last = this.min.child.prev;
        }
        HeapNode curNode = this.min.child;
        boolean last = false;
        while (!last) {
            HeapNode prev = curNode;
            curNode.mark = false;
            curNode.parent = null;
            curNode = curNode.next;
            if (curNode.key==this.min.child.key){ last=true; curNode = prev;}


        }
        curNode.next = this.min.next;
        this.min.next.prev = curNode;
        this.min.prev.next = this.min.child;
        this.min.child.prev = this.min.prev;
    }

    public void cut(HeapNode deletedNode) {
        HeapNode parent = deletedNode.parent;
        deletedNode.parent = null;
        deletedNode.mark = false;
        parent.rank--;
        if (deletedNode.next.key == deletedNode.key) {  // deletedNode is the only deletedNode
            parent.child = null;
        } else {
            if (parent.child.key == deletedNode.key) {
                parent.child = deletedNode.next;
            }
            deletedNode.prev.next = deletedNode.next;
            deletedNode.next.prev = deletedNode.prev;
        }
        numOfCuts++;
        this.insertNode(deletedNode);
    }

    public void insertNode(HeapNode newNode) {
        if (this.isEmpty()) {
            this.last = newNode;
            this.min = newNode;
        } else {
            this.first.prev = newNode;
            newNode.next = this.first;
            newNode.prev = this.last;
            this.last.next = newNode;
            if (newNode.key < this.min.key) {
                this.min = newNode;
            }
        }
        this.first = newNode;
    }

    public void cascadingCut(HeapNode child, HeapNode parent) {
        this.cut(child);
        if (parent.parent != null) {
            if (!parent.mark) {
                parent.mark = true;
            } else {
                this.cascadingCut(parent, parent.parent);
            }
        }
    }

    public void consolidation() {
        int logN = (int) (Math.log10(this.size) / Math.log10(2));
        int sizeArray= Math.max(logN, this.last.rank+1);
        HeapNode[] ranksArray = new HeapNode[sizeArray + 1];

        ranksArray[this.first.rank] = this.first;
        HeapNode curNode = this.first.next;
        this.first.next = this.first;
        this.first.prev = this.first;
        while (curNode.key != first.key) {
            HeapNode nextNode = curNode.next;
            curNode.prev = curNode;
            curNode.next = curNode;
            while (ranksArray[curNode.rank] != null && ranksArray[curNode.rank].key != curNode.key) {
                HeapNode inBucket = ranksArray[curNode.rank];
                if (inBucket.key < curNode.key) {
                    this.connectSameRanks(inBucket, curNode, ranksArray);
                    curNode = inBucket;
                } else {
                    this.connectSameRanks(curNode, inBucket, ranksArray);
                }
            }
            ranksArray[curNode.rank] = curNode;
            curNode = nextNode;
        }
        this.first = null;
        for (HeapNode item : ranksArray) {
            if (item != null) {
                if (this.first == null) {
                    this.first = item;
                    this.min = item;
                } else {
                    this.last.next = item;
                    item.prev = this.last;
                    if (item.key < this.min.key) {
                        this.min = item;
                    }
                }
                this.last = item;
            }
        }
        if (this.first != null) {
            this.first.prev = this.last;
            this.last.next = this.first;
        }
    }

    public void connectSameRanks(HeapNode smallerKey, HeapNode biggerKey, HeapNode[] ranksArray) {
        if (smallerKey.rank == 0) {
            smallerKey.child = biggerKey;
            biggerKey.parent = smallerKey;
        } else {
            HeapNode orgChild = smallerKey.child;
            smallerKey.child = biggerKey;
            biggerKey.parent = smallerKey;
            biggerKey.next = orgChild;
            biggerKey.prev = orgChild.prev;
            orgChild.prev.next = biggerKey;
            orgChild.prev = biggerKey;
        }
        smallerKey.rank++;
        ranksArray[biggerKey.rank] = null;
        if (ranksArray[smallerKey.rank] == null) {
            ranksArray[smallerKey.rank] = smallerKey;
        }
        numOfLinks++;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public int markRec(HeapNode node) {
        int result = 0;
        if (!node.mark) {
            result++;
        }
        HeapNode childNode = node.child;
        for (int i = 0; i < node.rank; i++) {
            result += markRec(childNode);
            childNode = childNode.next;
        }
        return result;
    }
    public HeapNode getFirst(){
        return this.first;
    }
    public HeapNode getMin(){
        return this.min;
    }
    public HeapNode getLast(){
        return this.last;
    }
//
//    public void main(String[] args) {
//        FibonacciHeap fib = new FibonacciHeap();
//        for (int i = 0; i < 5; i++) {
//            fib.insert(2 * i);
//        }
//

//        System.out.println(fib.nonMarked());
//        fib.decreaseKey(fib.first, 1);
//        System.out.println(fib.min.key);
//        System.out.println(fib.first.key);
//        fib.decreaseKey(fib.min, 1);
//        System.out.println(fib.min.key);
//        System.out.println(Arrays.toString(fib.countersRep()));
//        fib.deleteMin();
//        System.out.println(Arrays.toString(fib.countersRep()));
//        System.out.println(totalLinks());
//        System.out.println(totalCuts());
//        //fib.deleteMin();
//        System.out.println(Arrays.toString(fib.countersRep()));
//        System.out.println(fib.nonMarked());
//        System.out.println(totalLinks());
//        System.out.println(totalCuts());
//        System.out.println(Arrays.toString(kMin(fib, 4)));


    /**
     * public class HeapNode
     * <p>
     * If you wish to implement classes other than FibonacciHeap
     * (for example HeapNode), do it in this file, not in another file.
     */
    static class HeapNode {

        public int key;
        public int rank;
        public boolean mark;
        public HeapNode child;
        public HeapNode parent;
        public HeapNode next;
        public HeapNode prev;
        public HeapNode copy;

        public HeapNode(int key) {
            this.key = key;
            this.mark = false;
            this.child = null;
            this.parent = null;
            this.next = this;
            this.prev = this;
            this.rank = 0;
        }

        public int getKey() {
            return this.key;
        }

        public int getRank(){
            return this.rank;
        }

        public boolean getMarked(){
            return this.mark;
        }
        public HeapNode getChild(){
            return this.child;
        }

        public HeapNode getParent(){
            return this.parent;
        }
        public HeapNode getNext(){
            return this.next;
        }

        public HeapNode getPrev(){
            return this.prev;
        }
    }

    public static void main(String[] args){
        FibonacciHeap_v2 heap = new FibonacciHeap_v2();
        heap.insert(Integer.MIN_VALUE);
        heap.insert(0);
        HeapNode node = heap.first;
        heap.delete(node);
        System.out.println(""+heap.first.key +""+  heap.min.key);
    }
//        int m = (int) Math.pow(2, 20);
////        long startTime = System.currentTimeMillis();
//        FibonacciHeap heap = new FibonacciHeap();
//        HeapNode[] help = new HeapNode[m];
//
//        for (int i=m-1; i>-2; i--){
//            heap.insert(i);
//            if (i!= -1){
//            help[i]= heap.first;}
//        }
//        heap.deleteMin();
//        int pow = m;
//        HeapNode node2 = help[m-2];
//        System.out.println("cut" +""+node2.key);
//
//        while (pow>=2){
//            HeapNode node = help[m-pow+1];
//            System.out.println("cut" +""+node.key);
//            heap.decreaseKey(node, m+1);
//
//
//            pow= pow/2;
//        }
//        heap.decreaseKey(node2,m+1);
////        long endTime = System.currentTimeMillis();
////        long elapsedTime = endTime - startTime;
////        System.out.println("Elapsed time in milliseconds: " + elapsedTime);
//        System.out.println(totalLinks());
//        System.out.println(totalCuts());
//        System.out.println(heap.potential());
//
//        System.out.println(Arrays.toString(heap.countersRep()));


}

