import pandas as pd

df = pd.read_csv('example copy.csv')

df.columns = ['Thread', 'ParallelRegion', 'ExecutionTime']

total_parallel_regions = df['ParallelRegion'].nunique()
print(f"Total Number of Parallel Regions: {total_parallel_regions}")

region_execution_time = df.groupby('ParallelRegion')['ExecutionTime'].sum().reset_index()

region_execution_time = region_execution_time.sort_values(by='ExecutionTime', ascending=False)

region_execution_time['CumulativePercentage'] = (
    region_execution_time['ExecutionTime'].cumsum() / region_execution_time['ExecutionTime'].sum()
) * 100

top_regions = region_execution_time[region_execution_time['CumulativePercentage'] <= 80]

print("\nParallel Regions Consuming 80% of Execution Time:")
print(top_regions[['ParallelRegion', 'ExecutionTime', 'CumulativePercentage']])

thread_execution_time = df.groupby('Thread')['ExecutionTime'].sum().reset_index()

Q1 = thread_execution_time['ExecutionTime'].quantile(0.25)
Q3 = thread_execution_time['ExecutionTime'].quantile(0.75)
IQR = Q3 - Q1

lower_bound = Q1 - 1.5 * IQR
upper_bound = Q3 + 1.5 * IQR


filtered_threads = thread_execution_time[
    (thread_execution_time['ExecutionTime'] >= lower_bound) & 
    (thread_execution_time['ExecutionTime'] <= upper_bound)
]

mean_execution_time = filtered_threads['ExecutionTime'].mean()


threshold = mean_execution_time * 0.10

imbalanced_threads = thread_execution_time[
    (thread_execution_time['ExecutionTime'] > mean_execution_time + threshold) |
    (thread_execution_time['ExecutionTime'] < mean_execution_time - threshold)
]

print("Filtered Threads (without outliers):")
print(filtered_threads)
print("\nMean Execution Time of Filtered Threads:", mean_execution_time)
print("\nThreads Causing Load Imbalance:")
print(imbalanced_threads)


def remove_outliers(group):
    Q1 = group['ExecutionTime'].quantile(0.25)
    Q3 = group['ExecutionTime'].quantile(0.75)
    IQR = Q3 - Q1
    lower_bound = Q1 - 1.5 * IQR
    upper_bound = Q3 + 1.5 * IQR
    return group[(group['ExecutionTime'] >= lower_bound) & (group['ExecutionTime'] <= upper_bound)]


filtered_df = df.groupby('ParallelRegion').apply(remove_outliers).reset_index(drop=True)


average_execution_times = filtered_df.groupby('ParallelRegion', as_index=False)['ExecutionTime'].mean()


average_execution_times.columns = ['ParallelRegion', 'AverageExecutionTime']

print("Average Execution Time for Each Parallel Region (without outliers):")
print(average_execution_times)


for _, imbalanced_thread in imbalanced_threads.iterrows():
    thread_id = imbalanced_thread['Thread']
    thread_df = filtered_df[filtered_df['Thread'] == thread_id]
    
    for _, row in thread_df.iterrows():
        parallel_region = row['ParallelRegion']
        execution_time = row['ExecutionTime']
        
        
        average_time = average_execution_times.loc[
            average_execution_times['ParallelRegion'] == parallel_region, 'AverageExecutionTime'
        ].values[0]
        
        
        if abs(execution_time - average_time) > 0.10 * average_time:
            print(f"Load imbalance detected: Thread {thread_id}, Parallel Region {parallel_region}, "
                  f"Execution Time {execution_time}, Average Execution Time {average_time}")
