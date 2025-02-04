This is my attempt to improve MergingSortedSplitterator created by Heinz Kabutz and described in his newsletter

https://www.javaspecialists.eu/archive/Issue289-MergingSortedSpliterator.html

My implementation uses PriorityQueue to select the smallest element amoung the input streams.

I sent the initial version to Heizn, and he justly pointed that my initial implamentation was not stable, 
and sent OrderTest, which shows that. I addressed that by implementing StablePriorityQueue.


The code includes original implementation from Heizn, his tests, my implementation, and a new JMH based test 
(PerformanceTestJMH).