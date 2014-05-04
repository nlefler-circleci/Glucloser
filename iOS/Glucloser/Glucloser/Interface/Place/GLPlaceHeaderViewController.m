//
// Created by Nathan Lefler on 5/4/14.
// Copyright (c) 2014 NLefler. All rights reserved.
//

#import "GLPlaceHeaderViewController.h"
#import "GLPlace.h"

@interface GLPlaceHeaderViewController ()

@property (nonatomic, strong) GLPlace *place;

@property (nonatomic, strong) UILabel *nameLabel;
@property (nonatomic, strong) UILabel *locationLabel;

@end

@implementation GLPlaceHeaderViewController

- (id)initWithPlace:(GLPlace *)place {
  self = [super initWithNibName:nil bundle:nil];
  if (self) {
    _place = place;

    _nameLabel = [[UILabel alloc] initWithFrame:CGRectZero];
    _nameLabel.font = [UIFont boldSystemFontOfSize:16.0];
    _locationLabel = [[UILabel alloc] initWithFrame:CGRectZero];
  }
  return self;
}

- (void)viewDidLoad {
  [super viewDidLoad];

  [self.view addSubview:self.nameLabel];
  [self.view addSubview:self.locationLabel];
}

- (void)viewWillAppear:(BOOL)animated {
  [super viewWillAppear:animated];

  self.nameLabel.text = @"Place Name";
  [self.nameLabel sizeToFit];

  self.locationLabel.text = @"Address";
  [self.locationLabel sizeToFit];
  CGRect locationFrame = self.locationLabel.frame;
  locationFrame.origin.y = CGRectGetMaxY(self.nameLabel.frame) + 10.0;
  self.locationLabel.frame = locationFrame;
}

@end