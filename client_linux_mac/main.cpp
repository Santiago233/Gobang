//
//  main.cpp
//  ReversiClient
//
//  Created by ganjun on 2018/3/5.
//  Copyright © 2018年 ganjun. All rights reserved.
//

#include "Reversi.h"

int main() {
    srand((unsigned int)(time(nullptr)));
    Reversi reversi = Reversi();
    reversi.gameStart();
    return 0;
}


