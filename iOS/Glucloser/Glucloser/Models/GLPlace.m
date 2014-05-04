//
// Created by Nathan Lefler on 5/4/14.
// Copyright (c) 2014 NLefler. All rights reserved.
//

#import "GLPlace.h"

@interface GLPlace ()

@property (nonatomic, strong) NSString *name;

@end

@implementation GLPlace

- (id)initWithName:(NSString *)name {
  self = [super init];
  if (self) {
    _name = [name copy];
  }
  return self;
}

@end