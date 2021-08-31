import pandas

# create a list of 100000 pairs with values (i,i) then output pairs as 'rising_limbo.csv'
pairs = [(i,i) for i in range(100_000)]
pandas.DataFrame(pairs).to_csv('rising_limbo.csv', header=False, index=False)
