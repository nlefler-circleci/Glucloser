//
// Created by Nathan Lefler on 5/2/14.
// Copyright (c) 2014 NLefler. All rights reserved.
//

#import "GLNewMealViewController.h"
#import "GLPlace.h"
#import "GLPlaceHeaderViewController.h"

@interface GLNewMealViewController () <UITableViewDataSource>

@property (nonatomic, strong) GLPlace *place;

@property (nonatomic, strong) GLPlaceHeaderViewController *placeHeader;
@property (nonatomic, strong) UITableView *foodsTableView;

@end

@implementation GLNewMealViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
  self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
  if (self) {
    _placeHeader = [[GLPlaceHeaderViewController alloc] initWithPlace:nil];

    _foodsTableView = [[UITableView alloc] initWithFrame:CGRectZero style:UITableViewStylePlain];
    _foodsTableView.dataSource = self;

    self.edgesForExtendedLayout = UIRectEdgeAll & ~UIRectEdgeTop;
  }
  return self;
}

- (void)viewDidLoad {
  [super viewDidLoad];

  [self.placeHeader willMoveToParentViewController:self];
  [self.view addSubview:self.placeHeader.view];
  [self addChildViewController:self.placeHeader];

  [self.view addSubview:self.foodsTableView];
}

- (void)viewWillAppear:(BOOL)animated {
  [super viewWillAppear:animated];

  CGFloat barOffset = CGRectGetHeight([UIApplication sharedApplication].statusBarFrame) +
      CGRectGetHeight(self.navigationController.navigationBar.frame);
  self.placeHeader.view.frame = CGRectMake(0, barOffset, CGRectGetWidth(self.view.bounds), 100.0);

  CGFloat tableHeight = CGRectGetHeight(self.view.bounds) - CGRectGetMaxY(self.placeHeader.view.frame);
  self.foodsTableView.frame = CGRectMake(0, CGRectGetMaxY(self.placeHeader.view.frame),
      CGRectGetWidth(self.view.bounds), tableHeight);

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