#!/bin/bash
set -e

mkdir -p data/glove
mkdir -p data/fasttext
echo "Downloading Glove"
wget https://dl.fbaipublicfiles.com/fasttext/vectors-wiki/wiki.en.zip -P data/fasttext
wget http://www-nlp.stanford.edu/data/glove.840B.300d.zip -P data/glove
unzip data/glove/glove.840B.300d.zip -d data/glove
unzip data/fasttext/wiki.en.zip -d data/fasttext
rm -rf data/glove/glove.840B.300d.zip
rm -rf data/fasttext/wiki.en.zip
