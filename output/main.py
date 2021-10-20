'''
@author Khiem Vinh Tran, Nguyen Thanh Tam, Hao Phu Phan
Please contact vinhkhiemt135@gmail.com or haophan.cs.uit@gmail.com
'''
import argparse
import torch



parser = argparse.ArgumentParser()
'''
Use in the framework and cannot remove.
'''
parser.add_argument('--task', default='train', help='train | validate | test | evaluate')

parser.add_argument('--data_dir', default='data/COVID', help='directory that store the data.')
parser.add_argument('--file_train', default='train.json', help='Training')
parser.add_argument('--file_val', default='dev.json', help='validation')
parser.add_argument('--file_test', default='test.json', help='Test')
parser.add_argument('--file_output', default='output.json', help='test output file')



if __name__ == '__main__':
    args = parse_args()
    print(args)