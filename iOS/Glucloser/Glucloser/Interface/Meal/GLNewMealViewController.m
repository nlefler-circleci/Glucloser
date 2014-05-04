//
// Created by Nathan Lefler on 5/2/14.
// Copyright (c) 2014 NLefler. All rights reserved.
//

#import "GLNewMealViewController.h"
#import "GLPlace.h"

@interface GLNewMealViewController () <UITableViewDataSource>

@property (nonatomic, strong) GLPlace *place;

@property (nonatomic, strong) UIView *placeHeader;
@property (nonatomic, strong) UITableView *foodsTableView;

@end

@implementation GLNewMealViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
  self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
  if (self) {
    _placeHeader = [[UIView alloc] initWithFrame:CGRectZero];
    _placeHeader.backgroundColor = [UIColor greenColor];

    _foodsTableView = [[UITableView alloc] initWithFrame:CGRectZero style:UITableViewStylePlain];
    _foodsTableView.dataSource = self;

    self.edgesForExtendedLayout = UIRectEdgeAll & ~UIRectEdgeTop;
  }
  return self;
}

- (void)viewDidLoad {
  [super viewDidLoad];

  self.placeHeader.frame = CGRectMake(0, 0, CGRectGetWidth(self.view.bounds), 150.0);
  [self.view addSubview:self.placeHeader];

  CGFloat tableHeight = CGRectGetHeight(self.view.bounds) - CGRectGetMaxY(self.placeHeader.frame);
  self.foodsTableView.frame = CGRectMake(0, CGRectGetMaxY(self.placeHeader.frame),
      CGRectGetWidth(self.view.bounds), tableHeight);
  [self.view addSubview:self.foodsTableView];

  [self.foodsTableView reloadData];
}

#pragma mark - GLSlideMenuMemberController
- (NSString *)slideMenuTitle {
  return @"New Meal";
}

#pragma mark - UITableViewDataSource
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
  return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
  return 3;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
  UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"FOODID"];
  if (!cell) {
    cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"FOODID"];
  }
  cell.textLabel.text = @"Food";

  return cell;
}

@end