
# coding: utf-8

# In[1]:

import random
import itertools
import csv
from statistics import variance as var
from operator import mul


# In[4]:

def gen_train_data(path_out='train_data.csv', d=3, part_rk=10, x_min=1, x_max=10, b_min=-10, b_max=10, eps_mean=0, eps_var=1):
    X_grid = [[random.uniform(x_min, x_max) for _ in range(part_rk)] for _ in range(d)]
    X = list(itertools.product(*X_grid))
    bb = [random.randint(b_min, b_max) for _ in range(d+1)]
    f = lambda xx : sum(map(mul, xx, bb[:-1])) + bb[-1]
    yy = [y + random.normalvariate(eps_mean, eps_var) for y in map(f, X)]
    
    with open(path_out, 'w') as f:
        writer = csv.writer(f, delimiter=' ')
        for (xx, y) in zip(X, yy):
            writer.writerow(list(xx) + [y])


# In[5]:

gen_train_data(path_out='train_data_d3_n1000.csv', d=3)


# In[ ]:



