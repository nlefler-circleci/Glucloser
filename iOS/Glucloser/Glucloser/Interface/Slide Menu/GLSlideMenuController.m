//
// Created by Nathan Lefler on 5/2/14.
// Copyright (c) 2014 NLefler. All rights reserved.
//

#import "GLSlideMenuController.h"

typedef NS_ENUM(NSInteger, GLSlideMenuControllerState) {
  GLSlideMenuControllerStateOpen,
  GLSlideMenuControllerStateClosed
};

@interface GLSlideMenuController () <UITableViewDataSource, UITableViewDelegate>

@property (nonatomic, strong) NSMutableArray * memberControllers;
@property (nonatomic, strong) UIBarButtonItem * openBarItem;
@property (nonatomic, assign) CGFloat menuWidth;
@property (nonatomic, assign) GLSlideMenuControllerState menuState;

@property (nonatomic, strong) UITableView * menuItemsTable;
@property (nonatomic, strong) UIViewController * currentlySelectedViewController;

@end

@implementation GLSlideMenuController

- (instancetype)initWithMenuWidth:(CGFloat)width {
  self = [super initWithNibName:nil bundle:nil];
  if (self) {
    _menuWidth = width;
    _memberControllers = [[NSMutableArray alloc] init];
//    _openBarItem = [[UIBarButtonItem alloc] initWithImage:nil
//                                                    style:UIBarButtonItemStylePlain
//                                                   target:self
//                                                   action:@selector(toggleMenu:)];
    _openBarItem = [[UIBarButtonItem alloc] initWithTitle:@"Menu" style:UIBarButtonItemStylePlain target:self action:@selector(toggleMenu:)];
    _menuItemsTable = [[UITableView alloc] initWithFrame:CGRectZero style:UITableViewStylePlain];
    _menuItemsTable.dataSource = self;
    _menuItemsTable.delegate = self;

    _menuState = GLSlideMenuControllerStateClosed;
  }
  return self;
}

- (void)viewDidLoad {
  [super viewDidLoad];

  CGRect tableFrame = self.view.bounds;
  tableFrame.size.width = self.menuWidth;
  tableFrame.origin.x = -self.menuWidth;
  self.menuItemsTable.frame = tableFrame;
  [self.view addSubview:self.menuItemsTable];
  CGFloat offset = CGRectGetHeight(self.navigationController.navigationBar.frame) +
      CGRectGetHeight([UIApplication sharedApplication].statusBarFrame);
  self.menuItemsTable.contentInset = UIEdgeInsetsMake(offset, 0.0, 0.0, 0.0);

  self.navigationItem.leftBarButtonItem = self.openBarItem;

  self.view.backgroundColor = [UIColor whiteColor];
}

#pragma mark - Public
- (void)addViewControllers:(NSArray *)viewControllers {
  if (![viewControllers count]) {
    return;
  }
  BOOL shouldSetFirstToCurrent = [self.memberControllers count] == 0;
  [self.memberControllers addObjectsFromArray:viewControllers];
  [self.menuItemsTable reloadData];

  if (shouldSetFirstToCurrent) {
    self.currentlySelectedViewController = [self.memberControllers firstObject];
  }
}

- (void)setOpenImage:(UIImage *)openImage {
  self.openBarItem.image = openImage;
}

#pragma mark - UITableViewDataSource
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
  return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
  return [self.memberControllers count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
  UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"ID"];
  if (!cell) {
    cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"ID"];
  }
  UIViewController * memberController = (UIViewController *)self.memberControllers[indexPath.row];
  cell.textLabel.text = [memberController slideMenuTitle];

  return cell;
}

#pragma mark - UITableViewDelegate
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
  UIViewController * memberController = (UIViewController *)self.memberControllers[indexPath.row];
  self.currentlySelectedViewController = memberController;

  [self toggleMenu:self];
}

#pragma mark - Helpers
- (void)toggleMenu:(id)sender {
  CGRect menuFrame = self.menuItemsTable.frame;
  GLSlideMenuControllerState newState;
  switch (self.menuState) {
    case GLSlideMenuControllerStateClosed:
    {
      menuFrame.origin.x = 0;
      newState = GLSlideMenuControllerStateOpen;
      break;
    }
    case GLSlideMenuControllerStateOpen:
    {
      menuFrame.origin.x = -CGRectGetWidth(menuFrame);
      newState = GLSlideMenuControllerStateClosed;
      break;
    }
  }
  GLSlideMenuController __weak * weakSelf = self;
  [UIView animateWithDuration:0.3 animations:^{
    self.menuItemsTable.frame = menuFrame;
  } completion:^(BOOL finished) {
    if (finished) {
      weakSelf.menuState = newState;
    }
  }];
}

- (void)setCurrentlySelectedViewController:(UIViewController *)currentlySelectedViewController {
  if (self.currentlySelectedViewController) {
    [self.currentlySelectedViewController willMoveToParentViewController:nil];
    [self.currentlySelectedViewController.view removeFromSuperview];
    [self.currentlySelectedViewController removeFromParentViewController];
  }

  _currentlySelectedViewController = currentlySelectedViewController;

  [self.currentlySelectedViewController willMoveToParentViewController:self];
  self.currentlySelectedViewController.view.frame = self.view.bounds;
  [self.view insertSubview:self.currentlySelectedViewController.view belowSubview:self.menuItemsTable];
  [self addChildViewController:self.currentlySelectedViewController];
}

@end